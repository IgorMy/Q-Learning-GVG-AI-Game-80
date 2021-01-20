package UHU.iharmyshkevich134.QLearningV1;

import java.util.HashMap;
import java.util.Random;
import core.game.StateObservation;
import ontology.Types;

/**
 * Clase principal de Q-Learning
 * @author Ihar Myshkevich
 */
public final class algoritmo {
	
	// Atributos de la clase
	private static double alpha = -1;
	private static double gamma = -1;
	private static HashMap<String,double[]> tablaq = new HashMap<String,double[]>();
	private static Random aleatorio = new Random(System.currentTimeMillis());
	private static String e_f_c = ""; // Variable para detectar errores
	
	/**
	 * Constructor de la clase que solo comprueba si alpha y gamma están inicializados correctamente
	 * @throws Exception
	 */
	public algoritmo() throws Exception {
		if(alpha == -1 || gamma == -1) {
			throw new Exception("Alpha y/o gamma no estan definidos {alpha: "+alpha+" ; gamma: "+gamma);
		}
	}
	
	/**
	 * Establece el valor de alpha
	 * @param alpha
	 */
	public static void setAlpha(double numero) {
		alpha = numero;
	}
	
	/**
	 * Establece el valor de gamma
	 * @param gamma
	 */
	public static void setGamma(double numero) {
		gamma = numero;
	}

	/**
	 * Devuelve la tablaq
	 * @return tablaq
	 */
	public static HashMap<String, double[]> getTablaq() {
		return tablaq;
	}
	
	/**
	 * Obtiene el valor de alpha
	 * @return alpha
	 */
	public static double getAlpha() {
		return alpha;
	}

	/**
	 * Obtiene el valor de gamma
	 * @return gamma
	 */
	public static double getGamma() {
		return gamma;
	}
	
	/**
	 * Determina el estado en el que se encuentra el agente
	 * @param StateObservation del instante
	 * @return String que representa al estado
	 */
	private String determinarEstado(StateObservation so) {
		String estado = "";
		int tipo;
		String buffer = "";
		for(int j=0;j<so.getObservationGrid()[0].length;j++ ) {
    	for(int i=0;i < so.getObservationGrid().length;i++) {
    		
    			if(so.getObservationGrid()[i][j].size() == 0) {
    				estado = estado + " - ";
    			}else {
    				
    				for(int h=0;h<so.getObservationGrid()[i][j].size();h++) {
    					tipo = so.getObservationGrid()[i][j].get(h).itype;
    					if(tipo == 10 || tipo == 16) {
    						estado = estado + " X ";
    						h+=10;
    					}else
    					if(tipo == 6) {
    						buffer = buffer + " A ";
    					}else
    					if(tipo == 7) {
    						buffer = buffer+ " V ";
    					}else
    					if(tipo == 9) {
    						buffer = buffer + " G ";
    					}else
    					if(tipo == 1) {
    						buffer = buffer + " R ";
    					}else if( tipo == 2 || tipo == 3 || (so.getObservationGrid()[i][j].size() == 1 && (tipo == 15 || tipo == 14 || tipo == 13)) ) {
    						if(buffer.equals("")) {
    							estado = estado + " - ";
    						}
    						h+=10;
    					}
    				}
    				if(!buffer.equals("")) {
    					if(buffer.equals(" A  V ") || buffer.equals(" V  A ")) {
    						buffer = " T ";
    					}else if(buffer.equals(" G  R ") || buffer.equals(" R  G ")) {
    						buffer = " H ";
    					}else if(buffer.equals(" G  A ") || buffer.equals(" A  G ")) {
    						buffer = " I ";
    					}else if(buffer.equals(" G  V ") || buffer.equals(" V  G ")) {
    						buffer = " B ";
    					}else if(!buffer.equals(" V ") && !buffer.equals(" A ") && !buffer.equals(" G ") && !buffer.equals(" R ")){
    						buffer = " N ";
    					}
    				}
    				estado = estado + buffer;
    				buffer = "";
    			}
    			}
    			estado = estado + "\n";
    		}
    	estado = estado + so.getAvatarHealthPoints();
    	return estado;
	}
	/*
	 *  Casilla vacia: Vacio
	 *  itype 10: Arbol (X)
	 *  itype 6: Tanque azul (A)
	 *  itype 7: Coche verde (V)
	 *  itype 9: Gasolina (G)
	 *  itype 1: Coche rojo (R)
	 */
	
	/**
	 * Determina la calidad de las acciones en un estado dado, si este no esta presente en la
	 * tablaq, se añade con pesos negativos entre -2...0.
	 * @param String estado
	 * @param StateObservation so a
	 * @return double[] que represent la calidad de las acciones
	 */ 
	private double[] determinarCalidadAcciones(String estado,StateObservation so) {
		double[] acciones; 
		if(tablaq.containsKey(estado)) {
			acciones = tablaq.get(estado);
		}else {
			int numAcciones = so.getAvailableActions().size()+1;
			acciones = new double[numAcciones];
			for(int i=0;i<numAcciones;i++) {
				acciones[i] = aleatorio.nextDouble()*(-0.00000002);
			}
			tablaq.put(estado, acciones);
		}
		return acciones;
	}
	
	/**
	 * Determina la acción optima
	 * @param double[] acciones
	 * @return Object[] -> 0: Q-valor 1: Posición
	 */
	private Object[] accionOptima(double[] acciones) {
		Object[] accion = new Object[2];
		int pos = 0;
		double valor = Double.NEGATIVE_INFINITY;
		for(int i=0;i<acciones.length;i++) {
			if(acciones[i]>valor) {
				pos = i;
				valor = acciones[i];
			}
		}
		accion[0] = valor;
		accion[1] = pos;
		return accion;
	}
	
	/**
	 * 
	 * @param so
	 * @param vida_actual
	 * @return
	 */
	private double determinarRecompensa(StateObservation so, int vida_actual) {
		double reward;
		if(so.isGameOver()) {
			if(so.getGameWinner().equals(Types.WINNER.PLAYER_WINS)) {
				reward = Double.POSITIVE_INFINITY;
			}else {
				reward = Double.NEGATIVE_INFINITY;
			}
		}else {
			if(so.getAvatarHealthPoints() > vida_actual) {
				reward = 100;
			}else {
				reward = -0.00000001;
			}
		}
		return reward;
	}
	
	/**
	 * 
	 * @param so
	 * @return
	 */
	private double determinarFuturoOptimo(StateObservation so) {
		double salida = 0;
		if(!so.isGameOver()) {
			// Determinamos el estado actual
				String estado_actual = determinarEstado(so);
				e_f_c = estado_actual;
				System.out.println(estado_actual);
				System.out.println();
			// Obtenemos las posibles acciones que puede realizar el agente 
				double[] acciones_estado_actual = determinarCalidadAcciones(estado_actual,so);
					
			// Determinamos la accion optima a realizar
				Object[] accion_estado_actual = accionOptima(acciones_estado_actual);
			
			salida = (double)(accion_estado_actual[0]);
		}else {
			e_f_c = "";
			if(so.getGameWinner().equals(Types.WINNER.PLAYER_WINS)) {
				salida = 100000;
			}else {
				salida = -100000;
			}
		}
		return salida;
	}
	
	/**
	 * 
	 * @param so
	 * @param accion
	 * @return
	 */
	private double[] determinarFuturo(StateObservation so,Types.ACTIONS accion) {
		// Variable de salida
			double[] futuro = new double[2];
		
		// Antes de avanzar obtenemos los puntos de vida del agente para su futuro uso
			int vida_actual = so.getAvatarHealthPoints();
			
		// Avanzamos al futuro usando el metodo advance
			so.advance(accion);
		
		// Determinamos la recompensa
			futuro[0] = determinarRecompensa(so,vida_actual);
		
		// Determinamos el valor futuro optimo
			futuro[1] = determinarFuturoOptimo(so);
			
		return futuro;
	};
	
	private void actualizarTablaQ(String estado,Object[] accion_estado_actual,double[] futuro, double[] acciones_estado_actual) {
		acciones_estado_actual[(int)(accion_estado_actual[1])] = ( 1 - alpha )*(double)(accion_estado_actual[0]) + alpha * (futuro[0] + gamma*futuro[1]);
		tablaq.put(estado, acciones_estado_actual);
	}
	
	/**
	 * Metodo principal de la clase que actualiza la tablaq y determina la siguiente acción
	 * a realizar
	 * @param StateObservation del estado actual
	 * @return Types.ACTIONS 
	 */
	public Types.ACTIONS determinarAccion(StateObservation so){
		// Obtenemos el estado en el que se encuentra el agente
			String estado_actual = determinarEstado(so);
			
			if(e_f_c != "") {
				if(!e_f_c.equals(estado_actual)) {
					System.out.println("!!! Error ¡¡¡");
				}
			}
			
		// Obtenemos las posibles acciones que puede realizar el agente 
			double[] acciones_estado_actual = determinarCalidadAcciones(estado_actual,so);
			
			
		// Determinamos la accion optima a realizar
			Object[] accion_estado_actual = accionOptima(acciones_estado_actual);
		
		// Determinamos la accion a realizar
			Types.ACTIONS accion_a_realizar;
			if((int)(accion_estado_actual[1]) == so.getAvailableActions().size()) {
				accion_a_realizar = Types.ACTIONS.ACTION_NIL;
			}else {
				accion_a_realizar = so.getAvailableActions().get((int)(accion_estado_actual[1]));
			}
			
		// Determinamos la recompensa y el valor del futuro estado óptimo
			double[] futuro = determinarFuturo(so, accion_a_realizar);
			
		// Actualizamos la tabla q
			actualizarTablaQ(estado_actual,accion_estado_actual,futuro,acciones_estado_actual);
			
			
		return accion_a_realizar;
	}
	
}
