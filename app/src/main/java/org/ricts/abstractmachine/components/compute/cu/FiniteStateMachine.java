package org.ricts.abstractmachine.components.compute.cu;

public class FiniteStateMachine {	// 'Context'	
	private State currentState;
  
	public FiniteStateMachine(){
		
	}
	
	public FiniteStateMachine(State initialState){
		currentState = initialState;
	}
	
	public State currentState(){
		return currentState;
	}
	
	public void doCurrentStateAction(){
		currentState.performAction();
	}
	
	// Current state can be specified
	// allowing fine grained control of nextState transitions
	public void setCurrentState(State state){
		currentState = state;
	}		
	
  // State transitions can be specified in State object
	// allowing automatic nextState transitions
	public void goToNextState(){
		currentState = currentState.nextState();
	}	
}
