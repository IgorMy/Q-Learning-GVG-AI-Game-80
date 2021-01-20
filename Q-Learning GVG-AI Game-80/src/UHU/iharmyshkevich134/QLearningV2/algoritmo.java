package UHU.iharmyshkevich134.QLearningV2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.IntStream;
import java.io.*;

import core.game.StateObservation;
import ontology.Types;
import ontology.Types.ACTIONS;

/**
 * Clase principal de Q-Learning
 * @author Ihar Myshkevich
 */
public final class algoritmo {
	
	// Atributos de la clase
	private static double alpha = -1;
	private static double gamma = -1;
	private static double porcentaje = 0;
	private static HashMap<String,double[]> tablaq = new HashMap<String,double[]>();
	private static Random aleatorio = new Random(System.currentTimeMillis());

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
	 * Almacena la tabla q en un archivo .csv
	 * @throws IOException
	 */
	public static void saveQtable() throws IOException {
		// Eliminación del archivo anterior
		File archivo = new File("tablaq.csv");
		archivo.delete();
		// Generación del nuevo archivo
		FileWriter fw = new FileWriter("tablaq.csv");
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);
		
		Object[] keys = tablaq.keySet().toArray();
		for(int i=0;i<keys.length;i++) {
			String key = keys[i].toString(); 
			double[] acciones = tablaq.get(key);
			for(int j=0;j<acciones.length;j++) {
				key+=","+acciones[j];
			}
			pw.println(key);
		}
		pw.flush();
	}

	/**
	 * Obtiene el porcentaje de acciones aleatorias
	 * @return
	 */
	public static double getPorcentaje() {
		return porcentaje;
	}

	/**
	 * Establece el porcentaje de acciones aleatorias
	 * @param porcentaje
	 */
	public static void setPorcentaje(double porcentaje) {
		algoritmo.porcentaje = porcentaje;
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
	 * Devuelve la posición segura
	 * @param posicion_segura
	 * @param posicion_jugador_y
	 * @return
	 */
	private double[] determinarPosicionSegura(int[] posicion_segura, double posicion_jugador_x) {
		
		posicion_jugador_x--;
		ArrayList<Double> distancia = new ArrayList<Double>();
		ArrayList<Integer> p_s = new ArrayList<Integer>();
		double[] salida = new double[3];
		int p;
		
		for(int i=0;i<posicion_segura.length;i++) {
			if(posicion_segura[i]==0) {
				p_s.add(i);
				distancia.add(Math.abs(i-posicion_jugador_x));
			}
		}
		
		double minimo = Collections.min(distancia);
		
		for(int i=0;i<distancia.size();i++) {
			if(distancia.get(i) > minimo) {
				distancia.remove(i);
				p_s.remove(i);
				i--;
			}
		}
		
		if(p_s.size()==1) {
			p=0;
		}else {
			p = aleatorio.nextInt(p_s.size());
		}
		
		if(p_s.get(p) == posicion_jugador_x) {
			salida[0] = 1;
		}else if(p_s.get(p) < posicion_jugador_x) {
			salida[0] = 2;
		}else {
			salida[0] = 3;
		}
		salida[1] = distancia.get(p);
		salida[2] = p_s.get(p);
		return salida;
	}
	
	/**
	 * Determina el estado en el que se encuentra el agente
	 * @param StateObservation del instante
	 * @return String que representa al estado
	 */
	private int[] determinarEstado(StateObservation so) {
		int[] estado = new int[4];
		int posicion_jugador_y = (int)(so.getAvatarPosition().y/so.getBlockSize())+1;
		double posicion_jugador_x = so.getAvatarPosition().x/so.getBlockSize();
		int[] posicion_segura = new int[so.getObservationGrid().length-2];
		int[][] mapa = new int[so.getObservationGrid().length-2][posicion_jugador_y];
		ArrayList<int[]> obstaculos = new ArrayList<int[]>();
		
		// Gasolina
		if(so.getAvatarHealthPoints() <= 10) {
			estado[0]=1;
		}
		
		// Mapa cons obstaculos y determinación de gasolina
		// Obstaculo = 2
		// Zona insegura 1
		int tipo;
		for(int j=0;j<posicion_jugador_y;j++ ) {
			for(int i=1;i < so.getObservationGrid().length-1;i++) {
    			if(so.getObservationGrid()[i][j].size() > 0) {
    				for(int h=0;h<so.getObservationGrid()[i][j].size();h++) {
    					tipo = so.getObservationGrid()[i][j].get(h).itype;
    					if(mapa[i-1][j] < 2) {
	    					if(tipo == 6 || tipo == 7) {
	    						obstaculos.add(new int[] {i,j});
	    						mapa[i-1][j]=2;
	    					}else
	    					if(tipo == 9) {
	    						if(i == posicion_jugador_x) {
	    							estado[2] = 1;
	    						}else if(i < posicion_jugador_x) {
	    							estado[2] = 2;
	    						}else {
	    							estado[2] = 3;
	    						}
	    					}else if(tipo == 1) {
	    						mapa[i-1][j] = 9;
	    					}
    					}
    				}
    			}
    		}
    	}
		
		if(obstaculos.size()>0) {
			// Es necesario encontrar una posición segura local
			posicion_segura = new int[so.getObservationGrid().length-2];
			
			int pos_y = obstaculos.get(obstaculos.size()-1)[1]-1;
			boolean seguir = true;
			int itr = obstaculos.size()-1;
			while(seguir) {
				if(obstaculos.get(itr)[1] < pos_y) {
					seguir = false;
				}else {
					posicion_segura[obstaculos.get(itr)[0]-1] = 1;
					itr--;
					if(itr==-1) {
						seguir=false;
					}
				}
			}
			
			
			for(int i=0;i<posicion_segura.length;i++) {
				if(posicion_segura[i]==1) {
					if(i>0 && posicion_segura[i-1] == 0){
						posicion_segura[i-1] = 2;
					}
					if(i<posicion_segura.length-1 && posicion_segura[i+1] == 0) {
						posicion_segura[i+1]=2;
					}
				}
			}
			
			for(int i=0;i<posicion_segura.length;i++) {
				if(posicion_segura[i]==0) {
					posicion_segura[i]=1;
				}
				if(posicion_segura[i]==2) {
					posicion_segura[i]=0;
				}
			}
		}	
		
		if(IntStream.of(posicion_segura).sum() < posicion_segura.length) {
			double[] pos = determinarPosicionSegura(posicion_segura,posicion_jugador_x);
			estado[1] = (int)(pos[0]);
			
			int y = 0;
			// Determinar zona segura
			for(int i=0;i<obstaculos.size();i++) {
				if(pos[2] > (posicion_jugador_x-1)) {
					if(obstaculos.get(i)[0] >= posicion_jugador_x && (obstaculos.get(i)[0]-1) < pos[2] && obstaculos.get(i)[1] > y) {
						y = obstaculos.get(i)[1];
					}
				}else if(pos[2] < (posicion_jugador_x-1)){
					if(obstaculos.get(i)[0] <= posicion_jugador_x && (obstaculos.get(i)[0]-1) > pos[2] && obstaculos.get(i)[1] > y) {
						y = obstaculos.get(i)[1];
					}
				}else {
					if(obstaculos.get(i)[0] <= posicion_jugador_x+1 && (obstaculos.get(i)[0]-1) > pos[2]-1 && obstaculos.get(i)[1] > y) {
						y = obstaculos.get(i)[1];
					}
				}
			}
			
			if(posicion_jugador_y-pos[1]-1 <= y) {
				estado[3]=1;
			}
			
			if(posicion_jugador_y-2 == y) {
				estado[2] = 0;
			}
		}
		
		return estado;
	}

	/*
	 *  Casilla vacia: Vacio
	 *  itype 10: Arbol (X)
	 *  itype 6: Tanque azul (A)
	 *  itype 7: Coche verde (V) - 2 iteraciones para moverse (redondearemos a 1)
	 *  itype 9: Gasolina (G) - 4 iteraciones para moverse (redondearemos a 1)
	 *  itype 1: Coche rojo (R)
	 */
	
	/**
	 * Determina la calidad de las acciones en un estado dado, si este no esta presente en la
	 * tablaq, se añade con pesos negativos entre -2...0.
	 * @param String estado
	 * @param StateObservation so a
	 * @return double[] que represent la calidad de las acciones
	 */ 
	private double[] determinarCalidadAcciones(int[] estado,StateObservation so) {
		double[] acciones; 
		String estadoS = "";
		for(int i=0;i<estado.length;i++) {
			estadoS+=estado[i];
		}
		if(tablaq.containsKey(estadoS)) {
			acciones = tablaq.get(estadoS);
		}else {
			int numAcciones = so.getAvailableActions().size()+1-2;
			acciones = new double[numAcciones];
			for(int i=0;i<numAcciones;i++) {
				acciones[i] = aleatorio.nextDouble()*(-0.00000002);
			}
			tablaq.put(estadoS, acciones);
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
	 * Devuelve la recompensa obtenida por ejecutar la accion "accion" en el estado estado_actual
	 * @param so
	 * @param vida_actual
	 * @return
	 */
	private double determinarRecompensa(int[] estado_actual,ACTIONS accion) {
		return zona_segura(estado_actual,accion);
	}
	
	/**
	 * Devuelve el valor del futuro optimo
	 * @param so
	 * @return
	 */
	private double determinarFuturoOptimo(StateObservation so) {
		double salida = 0;
		if(!so.isGameOver()) {
			// Determinamos el estado actual
				int[] estado_actual = determinarEstado(so);
				
			// Obtenemos las posibles acciones que puede realizar el agente 
				double[] acciones_estado_actual = determinarCalidadAcciones(estado_actual,so);
					
			// Determinamos la accion optima a realizar
				Object[] accion_estado_actual = accionOptima(acciones_estado_actual);
			
			salida = (double)(accion_estado_actual[0]);
		}else {
			salida = 0;
		}
		return salida;
	}
	
	/**
	 * Determina el futuro y devuelve la recompensa de la accion y el valor de la accion futura optima
	 * @param so
	 * @param accion
	 * @return
	 */
	private double[] determinarFuturo(StateObservation so,Types.ACTIONS accion, int[] estado_actual) {
		// Variable de salida
			double[] futuro = new double[2];
			
		// Avanzamos al futuro usando el metodo advance
			so.advance(accion);
		
		// Determinamos la recompensa
			futuro[0] = determinarRecompensa(estado_actual,accion);
		
		// Determinamos el valor futuro optimo
			futuro[1] = determinarFuturoOptimo(so);
			
		return futuro;
	};
	
	/**
	 * Determinación de recompensa
	 * @param estado_actual
	 * @param accion
	 * @return
	 */
	private double zona_segura(int[] estado_actual,ACTIONS accion) {
		if(estado_actual[3]==0) {
			return gasolina(estado_actual,accion);
		}else {
			return posicion_segura(estado_actual,accion);
		}
	}
	
	/**
	 * Determinación de recompensa
	 * @param estado_actual
	 * @param accion
	 * @return
	 */
	private double gasolina(int[] estado_actual,ACTIONS accion) {
		if(estado_actual[0]==0) {
			return posicion_segura(estado_actual,accion);
		}else {
			return posicion_gasolina(estado_actual,accion);
		}
	}
	
	/**
	 * Determinación de recompensa
	 * @param estado_actual
	 * @param accion
	 * @return
	 */
	private double posicion_gasolina(int[] estado_actual,ACTIONS accion) {
		if( estado_actual[2] == 0 ){
			return posicion_segura(estado_actual,accion);
		}else if( estado_actual[2] == 1 && accion.equals(Types.ACTIONS.ACTION_NIL)) {
			return 5;
		}else if(  estado_actual[2] == 2  && accion.equals(Types.ACTIONS.ACTION_LEFT) ) {
			return 5;
		}else if(  estado_actual[2] == 3  && accion.equals(Types.ACTIONS.ACTION_RIGHT) ) {
			return 5;
		}else {
			return -5;
		}
	}
	
	/**
	 * Determinación de recompensa
	 * @param estado_actual
	 * @param accion
	 * @return
	 */
	private double posicion_segura(int[] estado_actual,ACTIONS accion) {
		if( ( estado_actual[1] == 0 || estado_actual[1] == 1 ) && accion.equals(Types.ACTIONS.ACTION_NIL)) {
			return 5;
		}else if(  estado_actual[1] == 2  && accion.equals(Types.ACTIONS.ACTION_LEFT) ) {
			return 5;
		}else if(  estado_actual[1] == 3  && accion.equals(Types.ACTIONS.ACTION_RIGHT) ) {
			return 5;
		}else {
			return -5;
		}
	}
	
	/**
	 * Actualiza la celda de la tabla Q
	 * @param estado
	 * @param accion_estado_actual
	 * @param futuro
	 * @param acciones_estado_actual
	 */
 	private void actualizarTablaQ(int[] estado,Object[] accion_estado_actual,double[] futuro, double[] acciones_estado_actual) {
 		String estadoS = "";
		for(int i=0;i<estado.length;i++) {
			estadoS+=estado[i];
		}
 		acciones_estado_actual[(int)(accion_estado_actual[1])] = ( 1 - alpha )*(double)(accion_estado_actual[0]) + alpha * (futuro[0] + gamma*futuro[1]);
		tablaq.put(estadoS, acciones_estado_actual);
	}
	
	/**
	 * Metodo principal de la clase que actualiza la tablaq y determina la siguiente acción
	 * a realizar
	 * @param StateObservation del estado actual
	 * @return Types.ACTIONS 
	 */
	public Types.ACTIONS determinarAccion(StateObservation so){
		ArrayList<ACTIONS> acciones = new ArrayList<ACTIONS>();
		ACTIONS accion_a_realizar;
		acciones.add(Types.ACTIONS.ACTION_NIL);
		acciones.add(Types.ACTIONS.ACTION_LEFT);
		acciones.add(Types.ACTIONS.ACTION_RIGHT);
		
		// Obtenemos el estado en el que se encuentra el agente
			int[] estado_actual = determinarEstado(so);
		
		// Obtenemos las posibles acciones que puede realizar el agente 
			double[] acciones_estado_actual = determinarCalidadAcciones(estado_actual,so);
		
		// Determinamos la accion optima a realizar
			Object[] accion_estado_actual = accionOptima(acciones_estado_actual);
			
		if(porcentaje < aleatorio.nextInt(100)+1) {
		// Determinamos la accion a realizar
			accion_a_realizar = acciones.get((int)(accion_estado_actual[1]));
		}else{
			accion_a_realizar = acciones.get(aleatorio.nextInt(3));
		}
			
		// Determinamos la recompensa y el valor del futuro estado óptimo
			double[] futuro = determinarFuturo(so, accion_a_realizar,estado_actual);
		
		// Actualizamos la tabla q
			actualizarTablaQ(estado_actual,accion_estado_actual,futuro,acciones_estado_actual);
			
		return accion_a_realizar;
	}

	/**
	 * Almacena datos que se le pasen por parametros en un fichero
	 * @param tiempo
	 * @param string
	 * @throws IOException
	 */
	public static void guardatDatos(ArrayList tiempo, String string) throws IOException {
		// Eliminación del archivo anterior
		File archivo = new File(string);
		archivo.delete();
		// Generación del nuevo archivo
		FileWriter fw = new FileWriter(string);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);

		String key = ""+tiempo.get(0);
		
		for(int i=1;i<tiempo.size();i++) {
			key += ","+tiempo.get(i);
		}
		pw.println(key);
		pw.flush();
	}

	/**
	 * Carga de tabla q
	 */
	public static void loadQTable() {
		try {
			  String[] values;
			  double[] acciones = new double[3];
		      File myObj = new File("tablaq.csv");
		      Scanner myReader = new Scanner(myObj);
		      while (myReader.hasNextLine()) {
		        values = myReader.nextLine().split(",");
		        for(int i=0;i<acciones.length;i++) {
		        	acciones[i] = Double.parseDouble(values[i+1]);
		        }
		        tablaq.put(values[0], acciones);
		      }
		      myReader.close();
		    } catch (FileNotFoundException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
		
	}
	
}
