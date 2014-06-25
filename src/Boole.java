import android.content.Context;
import android.widget.TableLayout;
import android.widget.TableRow;

public class Boole {

	/** Expresión original introducida por el usuario */
	private String original;
	
	/** Expresión optimizada introducida por el usuario */
	private String optimized;
	
	/** Variables que intervienen en la expresión */
	private String variables[];
	
	/** Variables únicas que intervienen en la expresión */
	private String variablesDistinct[];
	
	/** Combinaciones del mapa de karnaugh */
	public final static String[][] karnaughCombinations = {
		{},
		{"0", "1"},
		{"00", "01", "11", "10"},
		{"000", "001", "011", "010", "110", "111", "101", "100"},
		{"0000", "0001", "0011", "0010", "0110", "0111", "0101", "0100", "1100", "1101", "1111", "1110", "1010", "1011", "1001", "1000"},
		{"00000", "00001", "00011", "00010", "00110", "00111", "00101", "00100", "01100", "01101", "01111", "01110", "01010", "01011", "01001", "01000", "11000", "11001", "1011", "11010", "11110", "11111", "11101", "11100", "10100", "10101", "10111", "10110", "10010", "10011", "10001", "10000"}
	};
	
	/** CONSTRUCTOR */
	public Boole (String expression) {
		
		// Guardar expresión original
		this.original = expression;
		
		// Guardar expresión optimizada
		this.optimized = this.optimize();
		
		// Guardar variables que intervienen
		this.variables = this.getVariables();
		
		// Guardar variables únicas que intervienen
		this.variablesDistinct = this.getVariablesDistinct();
	}

	/** OBTENER ATRIBUTOS */
	public String getOriginalExpression () { return this.original; }
	public String getOptimizedExpression () { return this.optimized; }
	public String[] getVariablesExpression () { return this.variables; }
	
	/** Optimizar expresión */
	public String optimize () {
		
		String expression = this.original;
		
		// Eliminar espacios
		expression = expression.replaceAll("(\\s+)", "");
		
		// Negaciones
		expression = expression.replace("''", "");

		// Paréntesis
		expression = expression.replace("[", "(");
		expression = expression.replace("]", ")");
		expression = expression.replace(")'", "]");
		
		// ANDs
		expression = expression.replace("&&", "&");
	    expression = expression.replace("*", "&");
	    
	    // ORs
	    expression = expression.replace("||", "|");
		expression = expression.replace("+", "|");
		
		// Variables
		expression = expression.replaceAll("([A-Za-z0-9_]+)'", "\\!");
		expression = expression.replaceAll("([A-Za-z0-9_]+)", "\\$");

		// Devolver expresión
		return expression;
	}
	
	/** Obtener variables que intervienen en la expresión por orden */
	public String[] getVariables () {
		
		String expression = this.original;
		
		// Cambiar caracteres especiales por espacios
		expression = expression.replaceAll("[(\\(+)|(\\)+)|(\\[+)|(\\]+)|(\\*+)|(&+)|(\\++)|(\\|+)|('+)|(\\s+)]", " ");
		
		// Eliminar espacios redundantes
        expression = expression.replaceAll("(\\s+)", " ");
        
        // Eliminar espacio inicial, si existiese
        expression = expression.replaceAll("^\\s(\\.*)", "$1");
        
        // Devolver array de variables
        return expression.split("\\s");
	}
	
	/** Obtener variables únicas que intervienen en la expresión */
	public String[] getVariablesDistinct() {
		
		int i;
		int count = 0;
		String variablesTemp[] = new String[this.variables.length];
		
		// Inicializar variables temporales
        for (i = 0; i < variablesTemp.length; i++) { variablesTemp[i] = ""; }
		
		// Iterar variables
		for (i = 0; i < this.variables.length; i++) {
			
			// Comprobar si el elemento actual existe en las variables distintas
			if (!Boole.in_array(this.variables[i], variablesTemp)) { variablesTemp[count] = this.variables[i]; count++; }
			
		}
		
		// Crear array
		String variablesDistinct[] = new String[count];
		
		// Copiar array
		System.arraycopy(variablesTemp, 0, variablesDistinct, 0, count);
		
		// Devolver array de variables distintas
		return variablesDistinct;
	}
	
	/** Obtener función dual */
	public String dual () {
		
		int i;
		int count = 0;
		String c;
		String dual = "";
		
		// Iterar cadena
		for (i = 0; i < this.optimized.length(); i++) {
			
			// Caracter
			c = this.optimized.substring(i, i+1);
			
			// Si es una variable
			if (c.equals("$") || c.equals("!")) {
				
				// Sumar variable
				dual += this.variables[count];
				
				// Negar variable si es necesario
				if (c.equals("!")) { dual += "'"; }
				
				// Contador de variables
				count++;
				
			} else {
				
				// Sumar caracter
				dual += c;
				
			}
			
		}
		
		// Cambiar operaciones
		dual = dual.replace("&", " + ").replace("|", " * ");
		
		// Devolver función dual
		return dual;
	}
	
	/** Obtener f(vars) */
	public String fvars () {
		
		int i;
		String f = "f(";
		
		// Iterar variables distintas
		for (i = 0; i < this.variablesDistinct.length; i++) {
			
			// Añadir coma
			if (i != 0) { f += ", "; }
			
			// Añadir variable
			f += this.variablesDistinct[i];	
		}
		
		// cerrar función
		f += ")";
		
		// Devolver fvars
		return f;
		
	}
	
	/** Evaluar expresión para un conjunto de valores */
	public boolean eval (boolean values[]) {
		
		int i;															// Iterador
		boolean b;														// Valor booleano de la variable iterada
		String c;														// Caracter a evaluar
		int counterVars = 0;											// Contador de variables
		int openBrackets = 0;											// Contador de paréntesis abiertos
		boolean value[] = new boolean[this.optimized.length()];		// Valor
		boolean operation[] = new boolean[this.optimized.length()];	// Operación (false OR, true AND)
		
		// Inicializar operación
		value[0] = false;
		operation[0] = false;
		
		// Iterar cadena
		for (i = 0; i < this.optimized.length(); i++) {
			
			// Obtener subcadena
			c = this.optimized.substring(i, i+1);

			// Comprobar subcadena
			if (c.equals("|")) { operation[openBrackets] = false; }
			else if (c.equals("&")) { operation[openBrackets] = true; }
			else if (c.equals("$") || c.equals("!")) {

				// Obtener valor booleano de la variable
				b = values[this.indexOf(this.variables[counterVars])];

				// Negar valor si es necesario
				if (c.equals("!")) { b = !b; }

				// Actualizar resultado de la evaluación
				if (operation[openBrackets]) { value[openBrackets] = value[openBrackets] && b; } else { value[openBrackets] = value[openBrackets] || b; }

				// Sumar ocurrencia al contador
				counterVars++;

			} else if (c.equals("(")) {

				// Sumar al contador de paréntesis abiertos
				openBrackets++;

				// Inicializar operación
				value[openBrackets] = false;
				operation[openBrackets] = false;

			} else if (c.equals(")") || c.equals("]")) {
                
                // Negar valor si es necesario
                if (c.equals("]")) { value[openBrackets] = !value[openBrackets]; }

				// Restar al contador de paréntesis abiertos
				openBrackets--;
				
				// Operar al valor fuera de los paréntesis
				if (operation[openBrackets]) { value[openBrackets] = value[openBrackets] && value[openBrackets+1]; } else { value[openBrackets] = value[openBrackets] || value[openBrackets+1]; }
			}
		}
		
		// Devolver valor
		return value[0];
	}
	
	/** Evaluar expresión para todos los conjuntos de valores (2^n posibilidades) */
	public boolean[] eval () {
		
		int i, j;
		int n = (int) Math.pow(2, this.variablesDistinct.length);
		int weight;
		int count[] = new int[this.variablesDistinct.length];
		boolean combination[] = new boolean[this.variablesDistinct.length];
		boolean values[] = new boolean[n];
		
		// Inicializar combinación y cuenta
		for (i = 0; i < count.length; i++) { count[i] = 0; combination[i] = false; }
		
		// Iterar combinaciones
		for (i = 0; i < n; i++) {
			
			// Iterar valores para modificar combinación
			for (j = (combination.length - 1); j >= 0; j--) {
				
				// Actualizar peso
				weight = (int) Math.pow(2, combination.length - 1 - j);
				
				// Actualizar valor
				if (weight == count[j]) {
					
					// Reiniciar cuenta
					count[j] = 0;
					
					// Invertir valor
					combination[j] = !combination[j];
					
				}
				
				// Incrementar cuenta
				count[j]++;
			}
			
			// Evaluar combinación
			values[i] = this.eval(combination);
		}
		
		// Devolver valores
		return values;
	}
	
	/** Evaluar expresión para todos los conjuntos de valores (2^n posibilidades) y generar tabla */
	public void eval (Context context, TableLayout header, TableLayout table) {
		
		int i, j;
		int n = (int) Math.pow(2, this.variablesDistinct.length);
		int weight;
		int count[] = new int[this.variablesDistinct.length];
		boolean combination[] = new boolean[this.variablesDistinct.length];
		boolean values[] = new boolean[n];
		TableRow row, crow;
		
		// Crear fila
		row = new TableRow(context);
		row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));
		crow = new TableRow(context);
		crow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));
		
		// Inicializar combinación y cuenta
		for (i = 0; i < count.length; i++) {
			
			// Combinación y cuenta
			count[i] = 0; combination[i] = false;
			
			// Encabezado de la tabla
			TruthActivity.createCellTable(context, row, this.variablesDistinct[i], "#93d437", false, true);
			TruthActivity.createCellTable(context, crow, this.variablesDistinct[i], "#ffffff", false, false);
		}
		
		// Salida
		TruthActivity.createCellTable(context, row, context.getResources().getString(R.string.output), "#93d437", false, true);
		TruthActivity.createCellTable(context, crow, context.getResources().getString(R.string.output), "#e4ffbb", false, false);
		
		// Isertar fila de encabezado en la tabla
		header.addView(row);
		table.addView(crow);
		
		// Iterar combinaciones
		for (i = 0; i < n; i++) {
			
			// Crear fila
			row = new TableRow(context);
	        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));
			
			// Iterar valores para modificar combinación
			for (j = (combination.length - 1); j >= 0; j--) {
				
				// Actualizar peso
				weight = (int) Math.pow(2, combination.length - 1 - j);
				
				// Actualizar valor
				if (weight == count[j]) {
					
					// Reiniciar cuenta
					count[j] = 0;
					
					// Invertir valor
					combination[j] = !combination[j];
					
				}
				
				// Incrementar cuenta
				count[j]++;
				
				// Crear textview
				TruthActivity.createCellTable(context, row, Boole.parseBoolean(combination[j]), "#ffffff", true, true);
			}
			
			// Evaluar combinación
			values[i] = this.eval(combination);
			
			// Crear textview
			TruthActivity.createCellTable(context, row, Boole.parseBoolean(values[i]), "#e4ffbb", false, true);
			
			// Insertar fila en la tabla
			table.addView(row);
		}
	}
	
	/** Crear mapa de Karnaugh */
	public void karnaughMap (Context context, TableLayout table) {
		
		int i, j;
		int rows = (int) Math.floor((double) this.variablesDistinct.length / (double) 2);
		int columns = (int) Math.ceil((double) this.variablesDistinct.length / (double) 2);
		int prows = (int) Math.pow(2, rows);
		int pcolumns = (int) Math.pow(2, columns);
		TableRow row;
		
		// Crear fila
		row = new TableRow(context);
		row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));
		
		// Celda vacía (LEYENDA)
		KarnaughActivity.createCellTable(context, row, "", "#93d437", false);
		
		// Inicializar combinación y cuenta
		for (i = 0; i < pcolumns; i++) {
			
			// Encabezado de la tabla
			KarnaughActivity.createCellTable(context, row, Boole.karnaughCombinations[columns][i], "#93d437", false);
		}
		
		// Isertar fila de encabezado en la tabla
		table.addView(row);
		
		// Iterar filas
		for (i = 0; i < prows; i++) {
			
			// Crear fila
			row = new TableRow(context);
			row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));
			
			// Encabezado de la tabla
			KarnaughActivity.createCellTable(context, row, Boole.karnaughCombinations[rows][i], "#93d437", false);
			
			// Iterar columnas
			for (j = 0; j < pcolumns; j++) {
				
				// Encabezado de la tabla
				KarnaughActivity.createCellTable(context, row, Boole.parseBoolean(this.eval(Boole.parseString(Boole.karnaughCombinations[columns][j] + Boole.karnaughCombinations[rows][i]))), "#ffffff", false);
				
			}
			
			// Insertar fila en la tabla
			table.addView(row);
		}
	}
	
	/** Obtener minitérmino o maxitérmino de la función {false max, true min} */
	public String canonicalTerm (boolean maxmin, int term) {
		
		String canonicalTerm = "(";
		String binary = Boole.toBinaryString(term, this.variablesDistinct.length);
		String operation; if (!maxmin) { operation = "+"; } else { operation = "*"; }
		int i;
		
		// Iterar caracteres del número binario
		for (i = 0; i < binary.length(); i++) {
			
			// Añadir operación
			if (i != 0) { canonicalTerm += operation; }
			
			// Añadir variable al término
			canonicalTerm += this.variablesDistinct[i];
			
			// Negar término si es necesario
			if ((!maxmin && binary.substring(i, i+1).equals("1")) || (maxmin && binary.substring(i, i+1).equals("0"))) { canonicalTerm += "'"; }
		}
		
		// Cerrar término
		canonicalTerm += ")";
		
		// Devolver término canónico
		return canonicalTerm;
	}
	
	/** Obtener forma canónica de la función {false = Producto de sumas, true = Suma de productos} */
	public String canonicalForm (boolean maxmin) {
		
		boolean[] values = this.eval();
		String canonicalForm = "";
		String operation; if (!maxmin) { operation = " * "; } else { operation = " + "; }
		int i;
		
		// Iterar tabla de verdad
		for (i = 0; i < values.length; i++) {
			
			if ((!maxmin && !values[i]) || (maxmin && values[i])) {
				
				// Añadir operación
				if (!canonicalForm.equals("")) { canonicalForm += operation; }
				
				// Añadir término canónico
				canonicalForm += this.canonicalTerm(maxmin, i);
			}
		}
		
		// Devolver forma canónica
		return canonicalForm;
	}
	
	/** Simplificar función por Karnaugh (minitérminos) */
    public String karnaugh () {
    
        int i;
        String simplified = this.canonicalForm(true);
        
        // Simplificar n veces la función de n variables
        for (i = 0; i < this.variablesDistinct.length; i++) { simplified = Boole.simplifiedOneVariable(simplified); }
        
        // Devolver función simplificada
        return simplified;
    }
	
	/** Simplificar suma de productos en grupos de 2 (Karnaugh 2) */
	public static String simplifiedOneVariable (String function) {
		
		int i, j, k = 0;
		String simplified = "";
        String simplifiedTerms;
        String[] minterms = function.split("\\s\\+\\s");
        String[] simplifiedSuccess = new String[minterms.length*minterms.length];

		// Iterar minitérminos
		for (i = 0; i < minterms.length; i++) {
			
			// Combinar con minitérminos distintos
			for (j = i + 1; j < minterms.length; j++) {
			
				// Término simplificado
				simplifiedTerms = Boole.simplifiedMinterms(minterms[i], minterms[j], simplified, (!simplified.equals("")));
				                        
				// Si se ha podido simplificar
				if (!simplifiedTerms.equals("_unsimple_")) {
				
					// Añadir a los minitérminos simplificados
					simplifiedSuccess[k] = minterms[i]; k++;
					simplifiedSuccess[k] = minterms[j]; k++;
					
					// Añadir simplificación
					if (!simplifiedTerms.equals("_index_")) { simplified += simplifiedTerms; }
				}
			
			}
			
		}
        
        // Añadir los términos que no hayan sido simplificados
        for (i = 0; i < minterms.length; i++) {
            
            if (!Boole.in_array(minterms[i], simplifiedSuccess)) {
                
                // Operación
                if (!simplified.equals("")) { simplified += " + "; }

                // Término
                simplified += minterms[i];
            }
            
        }
	            
		// Devolver función simplificada (con una variables menos)
		return simplified;
	}
	
	/** Simplificar una variable de dos minitérminos de una expresión */
	public static String simplifiedMinterms (String min1, String min2, String expression, boolean operation) {
		
		int i;
		int changes = 0;
		String variable = "";
		String simplified = "";
		String[] terms1, terms2;
		
		// Separar variables de los minitérminos
		terms1 = min1.replace("(", "").replace(")", "").split("\\*");
		terms2 = min2.replace("(", "").replace(")", "").split("\\*");
		
		// Si los minitérminos tienen el mismo tamaño
		if (terms1.length == terms2.length) {
			
			// Iterar términos
			for (i = 0; i < terms1.length; i++) {
			
				if (terms1[i].equals(terms2[i] + "'") || terms2[i].equals(terms1[i] + "'")) { changes++; variable = terms1[i].replace("'", ""); }
				else if (!terms1[i].equals(terms2[i])) { changes = 0; break; }
				
			}
			
		}
		
		// Si ha habido un único cambio, simplificar
		if (changes == 1) { simplified = min1.replace(variable + "'", variable).replace("(" + variable + "*", "(").replace("*" + variable + ")", ")").replace("*" + variable + "*", "*"); }
		
		// Ya se ha simplificado | No se puede simplificar | Operación
		if ((expression.indexOf(simplified) != -1) && (changes == 1)) { simplified = "_index_"; }
        else if (changes != 1) { simplified = "_unsimple_"; }
        else if (operation) { simplified = " + " + simplified; }
		
		return simplified;
	}
	
	/** Convertir número decimal en binario */
	public static String toBinaryString (int number, int length) {
		
		String binary = "";
		
		while (number > 0) {	
			binary = (number % 2) + binary;
			number = (int) Math.floor(number / 2);
		}
		
		while (binary.length() < length) {
			binary = "0" + binary;
		}
		
		return binary;
		
	}
	
	/** Obtener posición de la variable */
	public int indexOf (String variable) {
		
		int i;
		
		// Iterar variables
		for (i = 0; i < this.variablesDistinct.length; i++) {
			
			// Comprobar si es la variable buscada
			if (this.variablesDistinct[i].equals(variable)) { return i; }
		}
		
		// Devolver posición
		return -1;
	}
	
	/** Saber si el elemento está en el array */
	public static boolean in_array (String variable, String array[]) {
		
		int i;
		
		// Iterar array
		for (i = 0; i < array.length; i++) {
			
			// Null
            if (array[i] == null) { array[i] = ""; }
			
			// Comprobar si la variable está en el array
			if (array[i].equals(variable)) { return true; }
		}
		
		// No está
		return false;
	}
	
	/** Pasar String a array booleano */
	public static boolean[] parseString (String value) {
		
		int i;
		boolean[] values = new boolean[value.length()];
		
		// Iterar caracteres
		for (i = 0; i < value.length(); i++) { values[i] = value.substring(i, i+1).equals("1"); }
		
		// Devolver conjunto de valores
		return values;
		
	}
	
	/** Pasar valor booleano a String (false = "0", true = "1") */
	public static String parseBoolean (boolean value) { if (value) { return "1";} else { return "0"; } }
}
