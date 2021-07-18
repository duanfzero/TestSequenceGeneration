package sequencingConstraints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.brics.automaton.Automaton;
import sequencingConstraints.Constraint;
import sequencingConstraints.TypeInfo;
import sequencingConstraints.constraintParser.ConstraintParser;

/**
 * sequencing constraint solver for CCSequence (Consecutive Complete Sequence) of events,
 * based on corresponding automaton translated from constraint
 * 
 * @author Feng Duan
 *
 */
public class SolverByAutomaton {
	boolean isDebugMode = false;
	
	private List<String> eventsList;
	
	private Constraint constraint;
	
	private int MAX_REPETITION; // max repetition of every event
	
	private int MAX_LENGTH; // max length of a test sequence
	
	private ConstraintToAutomaton convertor; // convert constraint to automaton
	
	private Automaton automaton; // the automaton should be generated before first using 
	
	public SolverByAutomaton(boolean isSolverDebugMode, 
			List<String> events, Constraint cons, int maxRepetition, int maxLength) {
		eventsList = events;
		
		constraint = cons;
		
		MAX_REPETITION = maxRepetition;
		
		MAX_LENGTH = maxLength;
		
		convertor = new ConstraintToAutomaton(eventsList);
		convertor.setDebugMode(isSolverDebugMode);
		automaton = convertor.convertSequencingExprToAutomaton(constraint);
	}
	
	// get the left-most event set in sequencing expression
	public Set<String> getLeftmostEventSet(Constraint expr){
		if (expr.getLeftOperand() == null) { // E
			return getEventSet(expr);
		}
		else if (expr.getOperator().equals("{")) // {E, E, ...}
			return getEventSet(expr);
		else if (expr.getOperator().equals("_")) // _ E
			return getEventSet(expr);
		else // expr with sequencing operator
			return getLeftmostEventSet(expr.getLeftOperand()); 
	}
	
	// get the set of all events in unary expression
	public Set<String> getEventSet(Constraint expr){
		// deep return first
		if (expr.getLeftOperand() == null) { // E => <event id>
			String event = expr.getOperator();
			Set<String> events = new LinkedHashSet<String>();
			events.add(event);
			return events;
		}
		else if (expr.getOperator().equals(",")) { // D => D, E => E, E, ...
			Set<String> leftSet = getEventSet(expr.getLeftOperand());
			Set<String> rightEvent = getEventSet(expr.getRightOperand());
			leftSet.addAll(rightEvent);
			return leftSet;
		}
		else { // C => {D}
			Set<String> leftSet = getEventSet(expr.getLeftOperand());
			return leftSet;
		}
	}
	
	public boolean solveSequencingExpr(List<String> ccSeq, Constraint sequencingExpr){
		return automaton.run(convertor.mappingSequenceToCharArray(ccSeq));
	}
	
	/**
	 * Do validity check on a CCSequence based on whole constraint to return valid or not. 
	 * (The CCSequence also should not exceed the Maximum Repetition of every event as Length Constraint.)
	 * 
	 * @param ccSeq
	 * @return
	 */
	public boolean solve(List<String> ccSeq){
		debugPrintln(ccSeq.toString());
		
		// Stop criterion as max length
		if (ccSeq.size() > MAX_LENGTH)
			return false;
		
		// Stop criterion as the Maximum Repetition of every event
		Map<String, Integer> eventCounter = new HashMap<String, Integer>();
		for (String event : ccSeq){
			if (!eventCounter.containsKey(event))
				eventCounter.put(event, 1);
			else {
				int count = eventCounter.get(event);
				eventCounter.put(event, count+1);
			}
		}
		for (Integer count : eventCounter.values()){
			if (count > MAX_REPETITION)
				return false;
		}
		
		// Solving constraint by recursively into solving operands, and then outside Boolean operators
		return solveConstraint(ccSeq, constraint);
	}
	
	public Boolean solveConstraint(List<String> ccSeq, Constraint cons)
	{
		return solveSequencingExpr(ccSeq, cons);
	}
	
	private void debugPrintln(String str)
	{
		if (isDebugMode)
			System.out.println(str);
	}

	/**
	 * @param isDebugMode the isDebugMode to set
	 */
	public void setDebugMode(boolean isDebugMode) {
		this.isDebugMode = isDebugMode;
	}

	/**
	 * @return the mAX_REPETITION
	 */
	public int getMAX_REPETITION() {
		return MAX_REPETITION;
	}

	/**
	 * @param mAX_REPETITION the mAX_REPETITION to set
	 */
	public void setMAX_REPETITION(int mAX_REPETITION) {
		MAX_REPETITION = mAX_REPETITION;
	}

	/**
	 * @return the mAX_LENGTH
	 */
	public int getMAX_LENGTH() {
		return MAX_LENGTH;
	}

	/**
	 * @param mAX_LENGTH the mAX_LENGTH to set
	 */
	public void setMAX_LENGTH(int mAX_LENGTH) {
		MAX_LENGTH = mAX_LENGTH;
	}

	/**
	 * @return the automaton
	 */
	public Automaton getAutomaton() {
		return automaton;
	}

	/**
	 * @return the convertor
	 */
	public ConstraintToAutomaton getConvertor() {
		return convertor;
	}
	
	public static void main(String[] args) {	
		// New motivating example {Open, Read, Write, Close}
		String[] events = {"Open", "Read", "Write", "Close"};
		
		/* Repetition Constraints */
		int maxRepetition = 2; // max repetition of every event
		
		/* Length Constraints */
		int maxLength = events.length * maxRepetition; // let Length Constraints to be the same as Repetition Constraints 
		
		/* Sequencing Constraints */
		List<String> constraints = new ArrayList<String>();
		String expr = "";
		
		expr = "Open +... Close"; // Cons1: an open file must be closed in time
		constraints.add(expr);
		
		expr = "(_ Open && Open .~. Close) -+ {Read, Write, Close}"; // Cons2: Read/Write/Close file operations require the file must be open
		constraints.add(expr);
		
		expr = "(_ Open && Open .~. Close) ~ Open"; // New constraint 3: assume we want Open must have corresponding Close, i.e., when the open file is not yet closed, no more Open event can be triggered
		constraints.add(expr);
		// New motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (int i=0; i<events.length; i++){
			eventsList.add(events[i]);
		}
		
		for (int i=0; i<constraints.size(); i++){
			String constraint = constraints.get(i);
			
			System.out.println("Events: ");
			System.out.println(eventsList);
			System.out.println("Sequencing Constraints: ");
			System.out.println(constraint);
			
			ConstraintParser parser = new ConstraintParser(
					constraint, eventsList);
			try {
				/* Parser */
				TypeInfo ti = parser.parse();

				String text = ti.getText();
				System.out.println("parser text: ");
				System.out.println(text);
				
				Constraint cons = ti.getConstraint();
				
				System.out.println("constraint in prefix expr: ");
				System.out.println(cons.plainText());
				
				/* ConstraintSolver */
				// Our constraints can be applied correctly on a complete test 
				// whose event's MaxRepetition > 1 (having self-loop or cyclic path)
				List<String> ccSeq = new ArrayList<String>(); // a consecutive complete sequence
				
//				// This seq should satisfy all three constraints
//				ccSeq.add("Open");
//				ccSeq.add("Read");
//				ccSeq.add("Close");
//				ccSeq.add("Open");
//				ccSeq.add("Close");
				
//				// This seq should violate the 1st constraint
//				ccSeq.add("Open");
//				ccSeq.add("Close");
//				ccSeq.add("Open");
//				ccSeq.add("Read");
				
//				// This seq should violate the 2nd constraint
//				ccSeq.add("Open");
//				ccSeq.add("Read");
//				ccSeq.add("Close");
//				ccSeq.add("Close");
				
				//  This seq should violate the 3rd constraint
				ccSeq.add("Open");
				ccSeq.add("Read");
				ccSeq.add("Open");
				ccSeq.add("Close");
				
				boolean isSolverDebugMode = false;
				SolverByAutomaton solver = new SolverByAutomaton(isSolverDebugMode, 
						eventsList, cons, maxRepetition, maxLength);
				boolean isValid = solver.solve(ccSeq);
				
				System.out.println("CCSequence : " + ccSeq);  
				if (isValid)
					System.out.println(" is valid.");
				else
					System.out.println(" is invalid!");
				System.out.println();
				
			} catch (Exception ex) {
				System.out.print(ex);
			}
		}
	}
	
}
