package QlearningController;

import java.util.ArrayList;
import java.util.Random;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;


public class Agent extends AbstractPlayer {
    
    protected Random randomGenerator;
    protected ArrayList<Types.ACTIONS> actions;

    private String mapa(StateObservation so)
    {
    	String mapa = "";
    	for(int i=0;i < so.getObservationGrid().length;i++) {
    		for(int j=0;j<so.getObservationGrid()[0].length;j++ ) {
    			if(so.getObservationGrid()[i][j].size()==0) {
    				mapa = mapa+0;
    			}else {
    				mapa =mapa+so.getObservationGrid()[i][j].get(0).itype;
    			}
    		}
    	}
    	return mapa;
    }

    // itype = 9 combustible
    // itype = 1 = coche
    // itype resto = arboles o coches
    
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        randomGenerator = new Random(System.currentTimeMillis());
        actions = so.getAvailableActions();
    }

    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
    	
    	
    	float[] acciones;
    	float learned_value;
    	
    	// Obtenemos el estado actual
    	String mapa = mapa(stateObs);
    	
    	// Comprobamos si esta en la tabla q, en el caso de que no este, se crea con valores aleatorios para la exploración
    	if(!QlearningObjects.tablaq.containsKey(mapa)) {
    		acciones = new float[actions.size()+1];
    		for(int i=0;i<actions.size()+1;i++) {
    			acciones[i] = randomGenerator.nextFloat()*(-2);
    			//System.out.println(acciones[i]);
    		}
    		QlearningObjects.tablaq.put(mapa, acciones);
    	}
    	
    	// En este punto elegimos la mejor acciónes
    	acciones = QlearningObjects.tablaq.get(mapa);
    	float Valor_Maximo =-3;
    	int pos = 0;
    	for(int i=0;i<actions.size()+1;i++) {
    		if(acciones[i] > Valor_Maximo) {
    			Valor_Maximo = acciones[i];
    			pos = i;
    		}
			
		}
    	
    	//System.out.println("Posicion valor maximo: "+pos+" Valor maximo: "+Valor_Maximo);
    	
    	int posicion_valor_q_a_modificar = pos;
    	
    	ACTIONS accion_a_realizar;
    	
    	if(pos == actions.size()){
    		accion_a_realizar = Types.ACTIONS.ACTION_NIL;
    		stateObs.advance(Types.ACTIONS.ACTION_NIL);
    	}else {
    		accion_a_realizar = actions.get(pos);
    		stateObs.advance(accion_a_realizar);
    	}
    	
    	if(!stateObs.isGameOver()) {
    		float reward;
    		int x = (int) (stateObs.getAvatarPosition().x/stateObs.getBlockSize());
    		int y = (int) (stateObs.getAvatarPosition().y/stateObs.getBlockSize());
    		if(stateObs.getObservationGrid()[x][y].size()==1) {
    			reward = -1;
    		}else {
    			reward = 0;
    		}
    		
    		String mapa2 = mapa(stateObs);
    		
    		if(!QlearningObjects.tablaq.containsKey(mapa2)) {
        		acciones = new float[actions.size()+1];
        		for(int i=0;i<actions.size()+1;i++) {
        			acciones[i] = randomGenerator.nextFloat()*(-2);
        		}
        		QlearningObjects.tablaq.put(mapa2, new float[actions.size()+1]);
        	}
    		
    		acciones = QlearningObjects.tablaq.get(mapa2);
    		Valor_Maximo =-3;
        	pos = 0;
        	for(int i=0;i<actions.size()+1;i++) {
        		if(acciones[i] > Valor_Maximo) {
        			Valor_Maximo = acciones[i];
        			pos = i;
        		}
    			
    		}
    		
    		learned_value = (reward) + QlearningObjects.gamma * Valor_Maximo;
    	}else {
    		if(stateObs.getGameWinner() == Types.WINNER.PLAYER_LOSES) {
    			learned_value = -100;
    		}else {
    			learned_value = -1;
    		}
    	}
    	
    	acciones = QlearningObjects.tablaq.get(mapa);
    	
    	acciones[posicion_valor_q_a_modificar] = (1-QlearningObjects.alpha)*acciones[posicion_valor_q_a_modificar]+QlearningObjects.alpha*learned_value;
    	
    	QlearningObjects.tablaq.put(mapa, acciones);
    	/*
    	try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	*/
        return accion_a_realizar;
    }

}
