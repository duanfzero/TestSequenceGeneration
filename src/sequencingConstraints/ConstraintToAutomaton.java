package sequencingConstraints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import generator.Subsequences;
import sequencingConstraints.Constraint;
import sequencingConstraints.TypeInfo;
import sequencingConstraints.constraintParser.ConstraintParser;
import util.Constants;

/**
 * translate sequencing constraint to automaton
 * 
 * @author Feng Duan
 *
 */
public class ConstraintToAutomaton {
	boolean isDebugMode = false;
	
	Set<String> events = new LinkedHashSet<String>();
	Set<Character> chars = new LinkedHashSet<Character>();
	
	Map<String, Character> mapEventsToChars = new HashMap<String, Character>();
	Map<Character, String> mapCharsToEvents = new HashMap<Character, String>();

	public ConstraintToAutomaton(List<String> eventsList) {
		for (String event : eventsList) {
			events.add(event);
		}
		
		char c = 'a'; // 'a' - 'z'
		for (String event : events) {
			chars.add(c);
			mapEventsToChars.put(event, c);
			mapCharsToEvents.put(c, event);
			c = (char) (c + 1);
		}
		
		System.out.println(events.toString());
		System.out.println(chars.toString());
		
		try {
			if (events.size() > 26)
				throw new Exception("Doesn't support more than 26 unique events!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public char mappingEventToChar(String event) {
		if (mapEventsToChars.containsKey(event)) {
			return mapEventsToChars.get(event);
		}
		else {
			try {
				throw new Exception("Doesn't support event " + event + "!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	public String mappingSequenceToCharArray(List<String> sequence) {
		char[] chars = new char[sequence.size()];
		for (int i=0; i<sequence.size(); i++) {
			String event = sequence.get(i);
			if (mapEventsToChars.containsKey(event)) {
				chars[i] = mapEventsToChars.get(event);
			}
			else {
				try {
					throw new Exception("Doesn't support event " + event + "!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		String charseq = "";
		for (int i=0; i<chars.length; i++)
			charseq += chars[i];
		
		return charseq;
	}
	
	public List<String> mappingCharArrayToSequence(char[] chars) {
		List<String> sequence = new ArrayList<String>();
		for (int i=0; i<chars.length; i++) {
			char c = chars[i];
			if (mapCharsToEvents.containsKey(c)) {
				String event = mapCharsToEvents.get(c);
				sequence.add(event);
			}
			else {
				String event = "epsilon";
				sequence.add(event);
			}
		}
		return sequence;
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
	
	/**
	 * get the set of all events in unary expression or event/set
	 * 
	 * @param expr a constraint that is an unary expression or event/set
	 * @return
	 */
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
	
	/**
	 * src --eventSet-> dest
	 * 
	 * @param src
	 * @param dest
	 * @param eventSet
	 */
	private void addTransitions(State src, State dest, Set<String> eventSet)
	{
		for (String event : events) {
			if (eventSet.contains(event)) {
				src.addTransition(new Transition(mappingEventToChar(event), dest));
			}
		}
	}
	
	// ALWAYS operator "_"
	// Updated to support not only event/set but also constraint as operand 20210313
	// _ B = NFA "e*Be*" when B equals to an automaton.
	public Automaton ALWAYSOperator(Constraint leftExpr){
		// anyAuto = "e*"
		Automaton anyAuto = new Automaton(); 
		State s1 = new State();
		s1.setAccept(true);			
		anyAuto.setInitialState(s1);
		// s1 --Any-> s1
		Set<String> Any = new HashSet<String>(events);
		addTransitions(s1, s1, Any);
		
		// B is the left automaton
		Automaton B = convertSequencingExprToAutomaton(leftExpr);
		
		// _ B = NFA "e*Be*"
		return anyAuto.concatenate(B).concatenate(anyAuto);
	}
	
	// IR operator "-+"
	// Updated to support not only event/set but also constraint as operand 20210313
	// B -+ C = !( !(B -+ C) ) = NFA ¡°!( !(e*B)Ce* )¡± when B and C equal to automata.
	public Automaton IROperator(Constraint leftExpr, Constraint rightExpr){
		Automaton automaton = new Automaton();
		
		// anyAuto = "e*"
		Automaton anyAuto = new Automaton(); 
		State s1 = new State();
		s1.setAccept(true);			
		anyAuto.setInitialState(s1);
		// s1 --Any-> s1
		Set<String> Any = new HashSet<String>(events);
		addTransitions(s1, s1, Any);
		
		// B is the left automaton
		Automaton B = convertSequencingExprToAutomaton(leftExpr);
		debugPrintln(leftExpr.plainText() + " to Automaton : ");
		debugPrintln(B.toDot());
		
		// C is the right automaton
		Automaton C = convertSequencingExprToAutomaton(rightExpr);
		debugPrintln(rightExpr.plainText() + " to Automaton : ");
		debugPrintln(C.toDot());
		
		// temp1 = "!(e*B)"
		Automaton temp1 = anyAuto.concatenate(B).complement();
		temp1.minimize();
		debugPrintln("temp1 = \"!(e*B)\" to Automaton : ");
		debugPrintln(temp1.toDot());
		
		// temp2 = "Ce*"
		Automaton temp2 = C.concatenate(anyAuto); 
		temp2.minimize();
		debugPrintln("temp2 = \"Ce*\" to Automaton : ");
		debugPrintln(temp2.toDot());
		
		// get the negation = "!(e*B)Ce*"
		Automaton negation = temp1.concatenate(temp2);
		negation.minimize();
		debugPrintln("negation = \"!(e*B)Ce*\" to Automaton : ");
		debugPrintln(negation.toDot());
		
		// the result automaton should be the complement of the negation
		automaton = negation.complement();
		// We have to clone the complement only with the mapping char set of events inside labels "\u0000-\uffff"
		automaton = cloneWithLimitedMinMax(automaton, events);
		
		return automaton;
	}
	
	// Deprecated GR operator "...+"
	// Updated to support not only event/set but also constraint as operand 20210313
	// B ...+ C = !( !(B ...+ C) ) = NFA ¡°!( !(e*Be*)Ce* )¡± when B and C equal to automata.
	public Automaton GROperator(Constraint leftExpr, Constraint rightExpr){
		Automaton automaton = new Automaton();
		
		// anyAuto = "e*"
		Automaton anyAuto = new Automaton(); 
		State s1 = new State();
		s1.setAccept(true);			
		anyAuto.setInitialState(s1);
		// s1 --Any-> s1
		Set<String> Any = new HashSet<String>(events);
		addTransitions(s1, s1, Any);
		
		// B is the left automaton
		Automaton B = convertSequencingExprToAutomaton(leftExpr);
		debugPrintln(leftExpr.plainText() + " to Automaton : ");
		debugPrintln(B.toDot());
		
		// C is the right automaton
		Automaton C = convertSequencingExprToAutomaton(rightExpr);
		debugPrintln(rightExpr.plainText() + " to Automaton : ");
		debugPrintln(C.toDot());
		
		// temp1 = "!(e*Be*)"
		Automaton temp1 = anyAuto.concatenate(B).concatenate(anyAuto).complement();
		temp1.minimize();
		debugPrintln("temp1 = \"!(e*B)\" to Automaton : ");
		debugPrintln(temp1.toDot());
		
		// temp2 = "Ce*"
		Automaton temp2 = C.concatenate(anyAuto); 
		temp2.minimize();
		debugPrintln("temp2 = \"Ce*\" to Automaton : ");
		debugPrintln(temp2.toDot());
		
		// get the negation = "!(e*Be*)Ce*"
		Automaton negation = temp1.concatenate(temp2);
		negation.minimize();
		debugPrintln("negation = \"!(e*Be*)Ce*\" to Automaton : ");
		debugPrintln(negation.toDot());
		
		// the result automaton should be the complement of the negation
		automaton = negation.complement();
		// We have to clone the complement only with the mapping char set of events inside labels "\u0000-\uffff"
		automaton = cloneWithLimitedMinMax(automaton, events);
		
		return automaton;
	}
	
	// IL operator "+-"
	// Updated to support not only event/set but also constraint as operand 20210313
	// B +- C = !( !(B +- C) ) = NFA ¡°!( e*B!(Ce*) )¡± when B and C equal to automata.
	public Automaton ILOperator(Constraint leftExpr, Constraint rightExpr){
		Automaton automaton = new Automaton();
		
		// anyAuto = "e*"
		Automaton anyAuto = new Automaton(); 
		State s1 = new State();
		s1.setAccept(true);			
		anyAuto.setInitialState(s1);
		// s1 --Any-> s1
		Set<String> Any = new HashSet<String>(events);
		addTransitions(s1, s1, Any);
		
		// B is the left automaton
		Automaton B = convertSequencingExprToAutomaton(leftExpr);
		debugPrintln(leftExpr.plainText() + " to Automaton : ");
		debugPrintln(B.toDot());
		
		// C is the right automaton
		Automaton C = convertSequencingExprToAutomaton(rightExpr);
		debugPrintln(rightExpr.plainText() + " to Automaton : ");
		debugPrintln(C.toDot());
		
		// temp1 = "e*B"
		Automaton temp1 = anyAuto.concatenate(B);
		temp1.minimize();
		debugPrintln("temp1 = \"e*B\" to Automaton : ");
		debugPrintln(temp1.toDot());
		
		// temp2 = "!(Ce*)"
		Automaton temp2 = C.concatenate(anyAuto).complement(); 
		temp2.minimize();
		debugPrintln("temp2 = \"!(Ce*)\" to Automaton : ");
		debugPrintln(temp2.toDot());
		
		// get the negation = "e*B!(Ce*)"
		Automaton negation = temp1.concatenate(temp2);
		negation.minimize();
		debugPrintln("negation = \"e*B!(Ce*)\" to Automaton : ");
		debugPrintln(negation.toDot());
		
		// the result automaton should be the complement of the negation
		automaton = negation.complement();
		// We have to clone the complement only with the mapping char set of events inside labels "\u0000-\uffff"
		automaton = cloneWithLimitedMinMax(automaton, events);
		
		return automaton;
	}
	
	// Deprecated GL operator "+..."
	// Updated to support not only event/set but also constraint as operand 20210313
	// B +... C = !( !(B +... C) ) = NFA ¡°!( e*B!(e*Ce*) )¡± when B and C equal to automata.
	public Automaton GLOperator(Constraint leftExpr, Constraint rightExpr){
		Automaton automaton = new Automaton();
		
		// anyAuto = "e*"
		Automaton anyAuto = new Automaton(); 
		State s1 = new State();
		s1.setAccept(true);			
		anyAuto.setInitialState(s1);
		// s1 --Any-> s1
		Set<String> Any = new HashSet<String>(events);
		addTransitions(s1, s1, Any);
		
		// B is the left automaton
		Automaton B = convertSequencingExprToAutomaton(leftExpr);
		debugPrintln(leftExpr.plainText() + " to Automaton : ");
		debugPrintln(B.toDot());
		
		// C is the right automaton
		Automaton C = convertSequencingExprToAutomaton(rightExpr);
		debugPrintln(rightExpr.plainText() + " to Automaton : ");
		debugPrintln(C.toDot());
		
		// temp1 = "e*B"
		Automaton temp1 = anyAuto.concatenate(B);
		temp1.minimize();
		debugPrintln("temp1 = \"e*B\" to Automaton : ");
		debugPrintln(temp1.toDot());
		
		// temp2 = "!(e*Ce*)"
		Automaton temp2 = anyAuto.concatenate(C).concatenate(anyAuto).complement(); 
		temp2.minimize();
		debugPrintln("temp2 = \"!(e*Ce*)\" to Automaton : ");
		debugPrintln(temp2.toDot());
		
		// get the negation = "e*B!(e*Ce*)"
		Automaton negation = temp1.concatenate(temp2);
		negation.minimize();
		debugPrintln("negation = \"e*B!(e*Ce*)\" to Automaton : ");
		debugPrintln(negation.toDot());
		
		// the result automaton should be the complement of the negation
		automaton = negation.complement();
		// We have to clone the complement only with the mapping char set of events inside labels "\u0000-\uffff"
		automaton = cloneWithLimitedMinMax(automaton, events);
		
		return automaton;
	}
	
	// IN operator "~"
	// Updated to support not only event/set but also constraint as operand 20210313
	// B ~ C = !( !(B ~ C) ) = NFA ¡°!( e*BCe* )¡± when B and C equal to automata.
	public Automaton INOperator(Constraint leftExpr, Constraint rightExpr){
		Automaton automaton = new Automaton();
		
		// anyAuto = "e*"
		Automaton anyAuto = new Automaton(); 
		State s1 = new State();
		s1.setAccept(true);			
		anyAuto.setInitialState(s1);
		// s1 --Any-> s1
		Set<String> Any = new HashSet<String>(events);
		addTransitions(s1, s1, Any);
		
		// B is the left automaton
		Automaton B = convertSequencingExprToAutomaton(leftExpr);
		debugPrintln(leftExpr.plainText() + " to Automaton : ");
		debugPrintln(B.toDot());
		
		// C is the right automaton
		Automaton C = convertSequencingExprToAutomaton(rightExpr);
		debugPrintln(rightExpr.plainText() + " to Automaton : ");
		debugPrintln(C.toDot());
		
		// temp1 = "e*B"
		Automaton temp1 = anyAuto.concatenate(B);
		temp1.minimize();
		debugPrintln("temp1 = \"e*B\" to Automaton : ");
		debugPrintln(temp1.toDot());
		
		// temp2 = "Ce*"
		Automaton temp2 = C.concatenate(anyAuto); 
		temp2.minimize();
		debugPrintln("temp2 = \"Ce*\" to Automaton : ");
		debugPrintln(temp2.toDot());
		
		// get the negation = "e*BCe*"
		Automaton negation = temp1.concatenate(temp2);
		negation.minimize();
		debugPrintln("negation = \"e*BCe*\" to Automaton : ");
		debugPrintln(negation.toDot());
		
		// the result automaton should be the complement of the negation
		automaton = negation.complement();
		// We have to clone the complement only with the mapping char set of events inside labels "\u0000-\uffff"
		automaton = cloneWithLimitedMinMax(automaton, events);
		
		return automaton;
	}
	
	// Deprecated GN operator ".~."
	// Updated to support not only event/set but also constraint as operand 20210313
	// B .~. C = !( !(B .~. C) ) = NFA ¡°!( e*Be*Ce* )¡± when B and C equal to automata.
	public Automaton GNOperator(Constraint leftExpr, Constraint rightExpr){
		Automaton automaton = new Automaton();
		
		// anyAuto = "e*"
		Automaton anyAuto = new Automaton(); 
		State s1 = new State();
		s1.setAccept(true);			
		anyAuto.setInitialState(s1);
		// s1 --Any-> s1
		Set<String> Any = new HashSet<String>(events);
		addTransitions(s1, s1, Any);
		
		// B is the left automaton
		Automaton B = convertSequencingExprToAutomaton(leftExpr);
		debugPrintln(leftExpr.plainText() + " to Automaton : ");
		debugPrintln(B.toDot());
		
		// C is the right automaton
		Automaton C = convertSequencingExprToAutomaton(rightExpr);
		debugPrintln(rightExpr.plainText() + " to Automaton : ");
		debugPrintln(C.toDot());
		
		// temp1 = "e*Be*"
		Automaton temp1 = anyAuto.concatenate(B).concatenate(anyAuto);
		temp1.minimize();
		debugPrintln("temp1 = \"e*Be*\" to Automaton : ");
		debugPrintln(temp1.toDot());
		
		// temp2 = "Ce*"
		Automaton temp2 = C.concatenate(anyAuto); 
		temp2.minimize();
		debugPrintln("temp2 = \"Ce*\" to Automaton : ");
		debugPrintln(temp2.toDot());
		
		// get the negation = "e*Be*Ce*"
		Automaton negation = temp1.concatenate(temp2);
		negation.minimize();
		debugPrintln("negation = \"e*Be*Ce*\" to Automaton : ");
		debugPrintln(negation.toDot());
		
		// the result automaton should be the complement of the negation
		automaton = negation.complement();
		// We have to clone the complement only with the mapping char set of events inside labels "\u0000-\uffff"
		automaton = cloneWithLimitedMinMax(automaton, events);
		
		return automaton;
	}
	
	/**
	 * Convert one sequencing expression into an Automaton. 
	 * 
	 * Notice that Nesting Feature allows Operands to be not only event/set but also constraint. 20210313
	 * 
	 * @param sequencingExpr  a sequencing expression which may have constraints as left or right operand.
	 */
	public Automaton convertSequencingExprToAutomaton(Constraint sequencingExpr){
		// If sequencingExpr is a single event or an event set,
		// then create events-corresponding automaton and return it.
		if (sequencingExpr.isEventOrSet()) {
			// get the event/set C1
			Set<String> C1 = getEventSet(sequencingExpr);
			
			// create and return automaton as Regex "C1"
			Automaton automaton = new Automaton();
			State s1 = new State();
			State s2 = new State();
			s2.setAccept(true);
			automaton.setInitialState(s1);
			// s1 --C1-> s2
			addTransitions(s1, s2, C1);
			return automaton;
		}
			
		// If sequencingExpr is more complex
		Automaton automaton = new Automaton();
		String op = sequencingExpr.getOperator();
		
		switch (op) {
		// Common operators on constraint can be mapping to the common operations on automata,
		// i.e., NOT "!" = the Complement of the automaton equivalent to constraint,
	    // CONCAT "." = the Concatenation of two automata, AND "&&" = Intersection, OR "||" = Union.
		case Constants.OR: // Left union Right
			automaton = convertSequencingExprToAutomaton(sequencingExpr.getLeftOperand()).
					union(convertSequencingExprToAutomaton(sequencingExpr.getRightOperand()));
			break;
		case Constants.AND: // Left intersection Right
			automaton = convertSequencingExprToAutomaton(sequencingExpr.getLeftOperand()).
					intersection(convertSequencingExprToAutomaton(sequencingExpr.getRightOperand()));
			break;
		case Constants.CONCAT: // Left concatenation Right
			automaton = convertSequencingExprToAutomaton(sequencingExpr.getLeftOperand()).
					concatenate(convertSequencingExprToAutomaton(sequencingExpr.getRightOperand()));
			break;
		case Constants.NOT: // the complement of Left automaton
			automaton = convertSequencingExprToAutomaton(sequencingExpr.getLeftOperand()).complement();
			// We have to clone the complement only with the mapping char set of events inside labels "\u0000-\uffff"
			automaton = cloneWithLimitedMinMax(automaton, events);
			break;
		
		// always sequencing operator, which only has leftExpr
		case Constants.ALWAYS:
			automaton = ALWAYSOperator(sequencingExpr.getLeftOperand());
			break;
			
		// immediately sequencing operators
		case Constants.IR:
			automaton = IROperator(sequencingExpr.getLeftOperand(), 
					sequencingExpr.getRightOperand());
			break;
		case Constants.IL:
			automaton = ILOperator(sequencingExpr.getLeftOperand(), 
					sequencingExpr.getRightOperand());
			break;
		case Constants.IN:
			automaton = INOperator(sequencingExpr.getLeftOperand(), 
					sequencingExpr.getRightOperand());
			break;
		
		// general sequencing operators
		case Constants.GR:
			automaton = GROperator(sequencingExpr.getLeftOperand(), 
					sequencingExpr.getRightOperand());
			break;
		case Constants.GL:
			automaton = GLOperator(sequencingExpr.getLeftOperand(), 
					sequencingExpr.getRightOperand());
			break;
		case Constants.GN:
			automaton = GNOperator(sequencingExpr.getLeftOperand(), 
					sequencingExpr.getRightOperand());
			break;
		default:
		}

		automaton.minimize();
		
		return automaton;
	}
	
	public boolean isExtendableinDFS(List<String> seq, int maxLength, int maxRepetition, Automaton automaton) {
		//  in extensibility check, seq can only reach maxLength - 1
		if (seq.size() >= maxLength)
			return false;
		
		// Stop criterion as the Maximum Repetition of every event
		Map<String, Integer> eventCounter = new HashMap<String, Integer>();
		for (int index=0; index<seq.size(); index++){
			String event = seq.get(index);
			if (!eventCounter.containsKey(event))
				eventCounter.put(event, 1);
			else {
				int count = eventCounter.get(event);
				eventCounter.put(event, count+1);
			}
		}
		for (Integer count : eventCounter.values()){
			if (count > maxRepetition)
				return false;
		}
		
		List<String> eventCandidates = new ArrayList<String>();
		eventCandidates.addAll(events);
		
		// follow pseudo-code in paper
		for (int i = 0; i < eventCandidates.size(); i++){
			List<String> tempSequence = new ArrayList<String>(seq);
			String event = eventCandidates.get(i);
			tempSequence.add(event);
			
			String runSeq = mappingSequenceToCharArray(tempSequence);
			
			if (automaton.run(runSeq)) {
//				debugPrintln("Valid sequence during extensibility checking: " + tempSequence);
				return true;
			}
			else {
//				debugPrintln("Invalid Sequence for extensibility checking: " + tempSequence);
				
				if (isExtendableinDFS(tempSequence, maxLength, maxRepetition, automaton))
					return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns a clone of automaton with the limitation of minChar and maxChar from eventSet.
	 * 
	 * @param original The original automaton which may contain labels such as "\u0000-\uffff"
	 * @param eventSet The given set of events such as "a, b, c, d"
	 * @return
	 */
	public Automaton cloneWithLimitedMinMax(Automaton original, Set<String> eventSet) {
		char minChar = Character.MAX_VALUE; // initial min is the largest value
		char maxChar = Character.MIN_VALUE; // initial max is the smallest value
		for (String event : eventSet) {
			char eventChar = mappingEventToChar(event);
			if (eventChar < minChar)
				minChar = eventChar;
			if (eventChar > maxChar)
				maxChar = eventChar;
		}
		
		Automaton a = new Automaton();
		HashMap<State, State> m = new HashMap<State, State>();
		Set<State> states = original.getStates();
		for (State s : states)
			m.put(s, new State());
		for (State s : states) {
			State p = m.get(s);
			p.setAccept(s.isAccept());
			if (original.getInitialState() == s)
				a.setInitialState(p);
			
			for (Transition t : s.getTransitions()) {
				char tMin = (char) Math.max(t.getMin(), minChar);
				char tMax = (char) Math.min(t.getMax(), maxChar);
				if (tMin <= tMax) {
					// only keep labels inside value field of the given set of events
					Transition tSimplified = new Transition(tMin, tMax, m.get(t.getDest()));
					p.addTransition(tSimplified);
				}
			}
		}
		return a;
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
	
}
