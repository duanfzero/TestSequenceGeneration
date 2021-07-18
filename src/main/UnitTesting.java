package main;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import generator.Subsequences;
import sequencingConstraints.Constraint;
import sequencingConstraints.ConstraintToAutomaton;
import sequencingConstraints.TypeInfo;
import sequencingConstraints.constraintParser.ConstraintParser;

/**
 * test sequencing constraints and generator
 * 
 * @author Feng Duan
 *
 */
public class UnitTesting {

	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void testGR1() {
		//motivating example
		String[] events = {"a", "b", "c"};
		
		List<String> constraints = new ArrayList<String>();
		String expr;
		
		expr = "a ...+ c";
		constraints.add(expr);
		
		/* Printed Result should be:
		Seq #0 [a, b, c, c] validity is true
		Seq #1 [b, c, a, b, c, c] validity is false
		*/
		String[][] seqStr = {
				{"a", "b", "c", "c"}, 
				{"b", "c", "a", "b", "c", "c"},
		};
		boolean[] expecteds = {true, false};
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
		
		int count = 0;
		Automaton intersectionAutomaton = null;
		for (int i=0; i<constraints.size(); i++){
			String constraint = constraints.get(i);
			
			System.out.println("Sequencing Constraint #" + i + ": ");
			System.out.println("  " + constraint);
			
			ConstraintParser parser = new ConstraintParser(constraint, eventsList);
			
			try {
				TypeInfo ti = parser.parse();
				
				Constraint sequencingConstraint = ti.getConstraint();
				
				ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
				Automaton automaton = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
				
				System.out.println(automaton.toDot());
				
				if (count == 0)
					intersectionAutomaton = automaton;
				else
					intersectionAutomaton = intersectionAutomaton.intersection(automaton);
				
				count++;
				
			} catch (Exception ex) {
				System.out.print(ex);
			}
		}
		
		System.out.println("Intersection Automaton is : ");
		System.out.println(intersectionAutomaton.toDot());
		
		ConstraintToAutomaton instance = new ConstraintToAutomaton(eventsList);
		
		List<List<String>> sequences = new ArrayList<List<String>>();
		for (int i=0; i<seqStr.length; i++) {
			List<String> sequence = new ArrayList<String>();
			for (int j=0; j<seqStr[i].length; j++) {
				sequence.add(seqStr[i][j]);
			}
			sequences.add(sequence);
		}
		
		int i = 0;
		boolean[] actuals = new boolean[sequences.size()];
		for (List<String> seq : sequences) {
			boolean isValid = intersectionAutomaton.run(instance.mappingSequenceToCharArray(seq));
			actuals[i] = isValid;
			
			System.out.println("Seq #" + i + " " + seq + " validity is " + isValid);
			i++;
		}
		System.out.println();
		
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testIR1() {
		//motivating example
		String[] events = {"a", "b", "c"};
		
		List<String> constraints = new ArrayList<String>();
		String expr;
		
		expr = "(_ a && a .~. b) -+ c";
		constraints.add(expr);
		
		// nested as ( (_e1 && e1 .~. e2) -+ e3 )
		// i.e., if e3 occurs, then e1 must occur before it, and e2 never occur between them.
		// e.g., [e1], [e2], [e1, e3, e2], [e1, e3, e3], [e1, e2, e1, e3] are accepted; 
		// [e3], [e1, e2, e3], [e1, e3, e2, e3] are rejected
		/* Printed Result should be:
		Seq #0 [a] validity is true
		Seq #1 [b] validity is true
		Seq #2 [a, c, b] validity is true
		Seq #3 [a, c, c] validity is true
		Seq #4 [a, b, a, c] validity is true
		Seq #5 [c] validity is false
		Seq #6 [a, b, c] validity is false
		Seq #7 [a, c, b, c] validity is false
		*/
		String[][] seqStr = {
				{"a"}, {"b"}, {"a", "c", "b"}, {"a", "c", "c"}, {"a", "b", "a", "c"},
				{"c"}, {"a", "b", "c"}, {"a", "c", "b", "c"},
		};
		boolean[] expecteds = {true, true, true, true, true, false, false, false};
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
		
		int count = 0;
		Automaton intersectionAutomaton = null;
		for (int i=0; i<constraints.size(); i++){
			String constraint = constraints.get(i);
			
			System.out.println("Sequencing Constraint #" + i + ": ");
			System.out.println("  " + constraint);
			
			ConstraintParser parser = new ConstraintParser(constraint, eventsList);
			
			try {
				TypeInfo ti = parser.parse();
				
				Constraint sequencingConstraint = ti.getConstraint();
				
				ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
				Automaton automaton = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
				
				System.out.println(automaton.toDot());
				
				if (count == 0)
					intersectionAutomaton = automaton;
				else
					intersectionAutomaton = intersectionAutomaton.intersection(automaton);
				
				count++;
				
			} catch (Exception ex) {
				System.out.print(ex);
			}
		}
		
		System.out.println("Intersection Automaton is : ");
		System.out.println(intersectionAutomaton.toDot());
		
		ConstraintToAutomaton instance = new ConstraintToAutomaton(eventsList);
		
		List<List<String>> sequences = new ArrayList<List<String>>();
		for (int i=0; i<seqStr.length; i++) {
			List<String> sequence = new ArrayList<String>();
			for (int j=0; j<seqStr[i].length; j++) {
				sequence.add(seqStr[i][j]);
			}
			sequences.add(sequence);
		}
		
		int i = 0;
		boolean[] actuals = new boolean[sequences.size()];
		for (List<String> seq : sequences) {
			boolean isValid = intersectionAutomaton.run(instance.mappingSequenceToCharArray(seq));
			actuals[i] = isValid;
			
			System.out.println("Seq #" + i + " " + seq + " validity is " + isValid);
			i++;
		}
		System.out.println();
		
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testIR2() {
		//motivating example
		String[] events = {"a", "b", "c"};
		
		List<String> constraints = new ArrayList<String>();
		String expr;
		
		expr = "(_ a && a +... b) -+ c"; // ( _ e1 && e1 +... e2) -+ e3
		constraints.add(expr);
		
		// nested as ( (_e1 && e1 .~. e2) -+ e3 )
		// i.e., if e3 occurs, then e1 must occur before it, and e2 never occur between them.
		// e.g., [e1], [e2], [e1, e3, e2], [e1, e3, e3], [e1, e2, e1, e3] are accepted; 
		// [e3], [e1, e2, e3], [e1, e3, e2, e3] are rejected
		/* Printed Result should be:
		Seq #0 [a, b, c, c] validity is true
		Seq #1 [a, b, a, c] validity is false
		Seq #2 [a, b, c, a, c] validity is false
		Seq #3 [a, c, b, c] validity is false
		*/
		String[][] seqStr = {
				{"a", "b", "c", "c"},
				{"a", "b", "a", "c"}, {"a", "b", "c", "a", "c"}, 
				{"a", "c", "b", "c"}, // this seq would be false-positive when dotStarNFA only check the last c as (e*Bc)
		};
		boolean[] expecteds = {true, false, false, false};
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
		
		int count = 0;
		Automaton intersectionAutomaton = null;
		for (int i=0; i<constraints.size(); i++){
			String constraint = constraints.get(i);
			
			System.out.println("Sequencing Constraint #" + i + ": ");
			System.out.println("  " + constraint);
			
			ConstraintParser parser = new ConstraintParser(constraint, eventsList);
			
			try {
				TypeInfo ti = parser.parse();
				
				Constraint sequencingConstraint = ti.getConstraint();
				
				ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
				Automaton automaton = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
				
				System.out.println(automaton.toDot());
				
				if (count == 0)
					intersectionAutomaton = automaton;
				else
					intersectionAutomaton = intersectionAutomaton.intersection(automaton);
				
				count++;
				
			} catch (Exception ex) {
				System.out.print(ex);
			}
		}
		
		System.out.println("Intersection Automaton is : ");
		System.out.println(intersectionAutomaton.toDot());
		
		ConstraintToAutomaton instance = new ConstraintToAutomaton(eventsList);
		
		List<List<String>> sequences = new ArrayList<List<String>>();
		for (int i=0; i<seqStr.length; i++) {
			List<String> sequence = new ArrayList<String>();
			for (int j=0; j<seqStr[i].length; j++) {
				sequence.add(seqStr[i][j]);
			}
			sequences.add(sequence);
		}
		
		int i = 0;
		boolean[] actuals = new boolean[sequences.size()];
		for (List<String> seq : sequences) {
			boolean isValid = intersectionAutomaton.run(instance.mappingSequenceToCharArray(seq));
			actuals[i] = isValid;
			
			System.out.println("Seq #" + i + " " + seq + " validity is " + isValid);
			i++;
		}
		System.out.println();
		
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testGL1() {
		//motivating example
		String[] events = {"a", "b", "c"};
		
		List<String> constraints = new ArrayList<String>();
		String expr;
		
		expr = "a +... c";
		constraints.add(expr);
		
		/* Printed Result should be:
		Seq #0 [c] validity is true
		Seq #1 [a, c] validity is true
		Seq #2 [a, c, c] validity is true
		Seq #3 [a, c, c, c] validity is true
		Seq #4 [a] validity is false
		Seq #5 [a, a, c] validity is true
		Seq #6 [a, b, c] validity is true
		Seq #7 [a, c, b] validity is true
		Seq #8 [a, c, a, b] validity is false
		*/
		String[][] seqStr = {
				{"c"}, {"a", "c"}, {"a", "c", "c"}, {"a", "c", "c", "c"}, 
				{"a"}, {"a", "a", "c"}, {"a", "b", "c"}, {"a", "c", "b"}, {"a", "c", "a", "b"}
		};
		boolean[] expecteds = {true, true, true, true, false, true, true, true, false};
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
		
		int count = 0;
		Automaton intersectionAutomaton = null;
		for (int i=0; i<constraints.size(); i++){
			String constraint = constraints.get(i);
			
			System.out.println("Sequencing Constraint #" + i + ": ");
			System.out.println("  " + constraint);
			
			ConstraintParser parser = new ConstraintParser(constraint, eventsList);
			
			try {
				TypeInfo ti = parser.parse();
				
				Constraint sequencingConstraint = ti.getConstraint();
				
				ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
				Automaton automaton = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
				
				System.out.println(automaton.toDot());
				
				if (count == 0)
					intersectionAutomaton = automaton;
				else
					intersectionAutomaton = intersectionAutomaton.intersection(automaton);
				
				count++;
				
			} catch (Exception ex) {
				System.out.print(ex);
			}
		}
		
		System.out.println("Intersection Automaton is : ");
		System.out.println(intersectionAutomaton.toDot());
		
		ConstraintToAutomaton instance = new ConstraintToAutomaton(eventsList);
		
		List<List<String>> sequences = new ArrayList<List<String>>();
		for (int i=0; i<seqStr.length; i++) {
			List<String> sequence = new ArrayList<String>();
			for (int j=0; j<seqStr[i].length; j++) {
				sequence.add(seqStr[i][j]);
			}
			sequences.add(sequence);
		}
		
		int i = 0;
		boolean[] actuals = new boolean[sequences.size()];
		for (List<String> seq : sequences) {
			boolean isValid = intersectionAutomaton.run(instance.mappingSequenceToCharArray(seq));
			actuals[i] = isValid;
			
			System.out.println("Seq #" + i + " " + seq + " validity is " + isValid);
			i++;
		}
		System.out.println();
		
		assertArrayEquals(expecteds, actuals);
	}
	
	
	@Test
	public void testIL1() {
		//motivating example
		String[] events = {"a", "b", "c"};
		
		List<String> constraints = new ArrayList<String>();
		String expr;
		
		expr = "(_ a && a .~. {b, c}) +- c"; // ( _ e1 && e1 .~. {e2, e3}) +- e3
		constraints.add(expr);
		
		// nested as ( ( _ e1 && e1 .~. {e2, e3}) +- e3 )
		// i.e., if ( _ e1 && e1 .~. {e2, e3}) is true in any subsequence (e1 occurs at the head and {e2, e3} never occur till the tail), 
		// then e3 must occur immediately after this subsequence.
		// e.g., [e2], [e3], [e1, e3], [e1, e3, e3], [e1, e3, e2], [e1, e3, e1, e3] are accepted; 
		// [e1], [e1, e2], [e1, e2, e3], [e1, e3, e1], [e1, e3, e1, e2] are rejected.
		// Note that, this constraint is very strict since some prefix of a not-e3 event would satisfy ( _ e1 && e1 .~. e2).
		/* Printed Result should be:
		Seq #0 [b] validity is true
		Seq #1 [c] validity is true
		Seq #2 [a, c] validity is true
		Seq #3 [a, c, c] validity is true
		Seq #4 [a, c, b] validity is true
		Seq #5 [a, c, a, c] validity is true
		Seq #6 [a] validity is false
		Seq #7 [a, b] validity is false
		Seq #8 [a, b, c] validity is false
		Seq #9 [a, c, a] validity is false
		Seq #10 [a, c, a, b] validity is false
		*/
		String[][] seqStr = {
				{"b"}, {"c"}, {"a", "c"}, {"a", "c", "c"}, {"a", "c", "b"}, {"a", "c", "a", "c"},
				{"a"}, {"a", "b"}, {"a", "b", "c"}, {"a", "c", "a"}, {"a", "c", "a", "b"}
		};
		boolean[] expecteds = {true, true, true, true, true, true, false, false, false, false, false};
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
		
		int count = 0;
		Automaton intersectionAutomaton = null;
		for (int i=0; i<constraints.size(); i++){
			String constraint = constraints.get(i);
			
			System.out.println("Sequencing Constraint #" + i + ": ");
			System.out.println("  " + constraint);
			
			ConstraintParser parser = new ConstraintParser(constraint, eventsList);
			
			try {
				TypeInfo ti = parser.parse();
				
				Constraint sequencingConstraint = ti.getConstraint();
				
				ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
				Automaton automaton = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
				
				System.out.println(automaton.toDot());
				
				if (count == 0)
					intersectionAutomaton = automaton;
				else
					intersectionAutomaton = intersectionAutomaton.intersection(automaton);
				
				count++;
				
			} catch (Exception ex) {
				System.out.print(ex);
			}
		}
		
		System.out.println("Intersection Automaton is : ");
		System.out.println(intersectionAutomaton.toDot());
		
		ConstraintToAutomaton instance = new ConstraintToAutomaton(eventsList);
		
		List<List<String>> sequences = new ArrayList<List<String>>();
		for (int i=0; i<seqStr.length; i++) {
			List<String> sequence = new ArrayList<String>();
			for (int j=0; j<seqStr[i].length; j++) {
				sequence.add(seqStr[i][j]);
			}
			sequences.add(sequence);
		}
		
		int i = 0;
		boolean[] actuals = new boolean[sequences.size()];
		for (List<String> seq : sequences) {
			boolean isValid = intersectionAutomaton.run(instance.mappingSequenceToCharArray(seq));
			actuals[i] = isValid;
			
			System.out.println("Seq #" + i + " " + seq + " validity is " + isValid);
			i++;
		}
		System.out.println();
		
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testIL2() {
		//motivating example
		String[] events = {"a", "b", "c"};
		
		List<String> constraints = new ArrayList<String>();
		String expr;
		
		expr = "(_ a && a +... b && a .~. c) +- c"; // ( _ e1 && e1 +... e2 && e1 .~. e3) +- e3
		constraints.add(expr);
		
		/* Printed Result should be:
		Seq #0 [a, c] validity is true
		Seq #1 [a, b, c] validity is true
		Seq #2 [a, a, b, c] validity is true
		Seq #3 [a, b, c, c] validity is true
		Seq #4 [a, b] validity is false
		Seq #5 [a, b, a] validity is false
		Seq #6 [a, b, b, c] validity is false
		*/
		String[][] seqStr = {
				{"a", "c"}, {"a", "b", "c"}, {"a", "a", "b", "c"}, {"a", "b", "c", "c"},
				{"a", "b"}, {"a", "b", "a"}, {"a", "b", "b", "c"}
		};
		boolean[] expecteds = {true, true, true, true, false, false, false};
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
		
		int count = 0;
		Automaton intersectionAutomaton = null;
		for (int i=0; i<constraints.size(); i++){
			String constraint = constraints.get(i);
			
			System.out.println("Sequencing Constraint #" + i + ": ");
			System.out.println("  " + constraint);
			
			ConstraintParser parser = new ConstraintParser(constraint, eventsList);
			
			try {
				TypeInfo ti = parser.parse();
				
				Constraint sequencingConstraint = ti.getConstraint();
				
				ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
				Automaton automaton = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
				
				System.out.println(automaton.toDot());
				
				if (count == 0)
					intersectionAutomaton = automaton;
				else
					intersectionAutomaton = intersectionAutomaton.intersection(automaton);
				
				count++;
				
			} catch (Exception ex) {
				System.out.print(ex);
			}
		}
		
		System.out.println("Intersection Automaton is : ");
		System.out.println(intersectionAutomaton.toDot());
		
		ConstraintToAutomaton instance = new ConstraintToAutomaton(eventsList);
		
		List<List<String>> sequences = new ArrayList<List<String>>();
		for (int i=0; i<seqStr.length; i++) {
			List<String> sequence = new ArrayList<String>();
			for (int j=0; j<seqStr[i].length; j++) {
				sequence.add(seqStr[i][j]);
			}
			sequences.add(sequence);
		}
		
		int i = 0;
		boolean[] actuals = new boolean[sequences.size()];
		for (List<String> seq : sequences) {
			boolean isValid = intersectionAutomaton.run(instance.mappingSequenceToCharArray(seq));
			actuals[i] = isValid;
			
			System.out.println("Seq #" + i + " " + seq + " validity is " + isValid);
			i++;
		}
		System.out.println();
		
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testGN1() {
		//motivating example
		String[] events = {"a", "b", "c"};
		
		List<String> constraints = new ArrayList<String>();
		String expr;
		
		expr = "a .~. c";
		constraints.add(expr);
		
		/* Printed Result should be:
		Seq #0 [c] validity is true
		Seq #1 [a, a, b] validity is true
		Seq #2 [b, b, c] validity is true
		Seq #3 [a, a, b, c] validity is false
		*/
		String[][] seqStr = {
				{"c"}, {"a", "a", "b"}, {"b", "b", "c"}, 
				{"a", "a", "b", "c"}
		};
		boolean[] expecteds = {true, true, true, false};
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
		
		int count = 0;
		Automaton intersectionAutomaton = null;
		for (int i=0; i<constraints.size(); i++){
			String constraint = constraints.get(i);
			
			System.out.println("Sequencing Constraint #" + i + ": ");
			System.out.println("  " + constraint);
			
			ConstraintParser parser = new ConstraintParser(constraint, eventsList);
			
			try {
				TypeInfo ti = parser.parse();
				
				Constraint sequencingConstraint = ti.getConstraint();
				
				ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
				Automaton automaton = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
				
				System.out.println(automaton.toDot());
				
				if (count == 0)
					intersectionAutomaton = automaton;
				else
					intersectionAutomaton = intersectionAutomaton.intersection(automaton);
				
				count++;
				
			} catch (Exception ex) {
				System.out.print(ex);
			}
		}
		
		System.out.println("Intersection Automaton is : ");
		System.out.println(intersectionAutomaton.toDot());
		
		ConstraintToAutomaton instance = new ConstraintToAutomaton(eventsList);
		
		List<List<String>> sequences = new ArrayList<List<String>>();
		for (int i=0; i<seqStr.length; i++) {
			List<String> sequence = new ArrayList<String>();
			for (int j=0; j<seqStr[i].length; j++) {
				sequence.add(seqStr[i][j]);
			}
			sequences.add(sequence);
		}
		
		int i = 0;
		boolean[] actuals = new boolean[sequences.size()];
		for (List<String> seq : sequences) {
			boolean isValid = intersectionAutomaton.run(instance.mappingSequenceToCharArray(seq));
			actuals[i] = isValid;
			
			System.out.println("Seq #" + i + " " + seq + " validity is " + isValid);
			i++;
		}
		System.out.println();
		
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	// test whether nesting NEVER operator works as purpose
	public void testGN2() {
		//motivating example
		String[] events = {"a", "b", "c"};
		
		List<String> constraints = new ArrayList<String>();
		String expr;
		
		// equal to RE "(b*(cb*a)*)*"
		expr = "c +... a && (_ c && c .~. a) ~ c" // when an event c doesn't has its corresponding a, no more c can occur
				+ " && (_ c && c .~. a) -+ a"; // and a can only occur after its corresponding c 
		constraints.add(expr);
		
		/* Printed Result should be:
		Seq #0 [c] validity is false
		Seq #1 [c, b, a] validity is true
		Seq #2 [c, a, c] validity is false
		Seq #3 [c, c, a, b] validity is false
		*/
		String[][] seqStr = {
				{"c"}, {"c", "b", "a"}, {"c", "a", "c"}, 
				{"c", "c", "a", "b"}
		};
		boolean[] expecteds = {false, true, false, false};
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
		
		int count = 0;
		Automaton intersectionAutomaton = null;
		for (int i=0; i<constraints.size(); i++){
			String constraint = constraints.get(i);
			
			System.out.println("Sequencing Constraint #" + i + ": ");
			System.out.println("  " + constraint);
			
			ConstraintParser parser = new ConstraintParser(constraint, eventsList);
			
			try {
				TypeInfo ti = parser.parse();
				
				Constraint sequencingConstraint = ti.getConstraint();
				
				ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
				Automaton automaton = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
				
				System.out.println(automaton.toDot());
				
				if (count == 0)
					intersectionAutomaton = automaton;
				else
					intersectionAutomaton = intersectionAutomaton.intersection(automaton);
				
				count++;
				
			} catch (Exception ex) {
				System.out.print(ex);
			}
		}
		
		System.out.println("Intersection Automaton is : ");
		System.out.println(intersectionAutomaton.toDot());
		
		ConstraintToAutomaton instance = new ConstraintToAutomaton(eventsList);
		
		List<List<String>> sequences = new ArrayList<List<String>>();
		for (int i=0; i<seqStr.length; i++) {
			List<String> sequence = new ArrayList<String>();
			for (int j=0; j<seqStr[i].length; j++) {
				sequence.add(seqStr[i][j]);
			}
			sequences.add(sequence);
		}
		
		int i = 0;
		boolean[] actuals = new boolean[sequences.size()];
		for (List<String> seq : sequences) {
			boolean isValid = intersectionAutomaton.run(instance.mappingSequenceToCharArray(seq));
			actuals[i] = isValid;
			
			System.out.println("Seq #" + i + " " + seq + " validity is " + isValid);
			i++;
		}
		System.out.println();
		
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testGN3() {
		//motivating example
		String[] events = {"a", "b", "c"};
		
		List<String> constraints = new ArrayList<String>();
		String expr;
		
		// equal to RE "c*a"
		expr = "_ a && {b, c} +... a && {a, b} .~. a";
		constraints.add(expr);
		
		/* Printed Result should be:
		Seq #0 [a] validity is true
		Seq #1 [c, c, a] validity is true
		Seq #2 [c, a, c] validity is false
		Seq #3 [c, c, a, b] validity is false
		*/
		String[][] seqStr = {
				{"a"}, {"c", "c", "a"}, {"c", "a", "c"}, 
				{"c", "c", "a", "b"}
		};
		boolean[] expecteds = {true, true, false, false};
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
		
		int count = 0;
		Automaton intersectionAutomaton = null;
		for (int i=0; i<constraints.size(); i++){
			String constraint = constraints.get(i);
			
			System.out.println("Sequencing Constraint #" + i + ": ");
			System.out.println("  " + constraint);
			
			ConstraintParser parser = new ConstraintParser(constraint, eventsList);
			
			try {
				TypeInfo ti = parser.parse();
				
				Constraint sequencingConstraint = ti.getConstraint();
				
				ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
				Automaton automaton = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
				
				System.out.println(automaton.toDot());
				
				if (count == 0)
					intersectionAutomaton = automaton;
				else
					intersectionAutomaton = intersectionAutomaton.intersection(automaton);
				
				count++;
				
			} catch (Exception ex) {
				System.out.print(ex);
			}
		}
		
		System.out.println("Intersection Automaton is : ");
		System.out.println(intersectionAutomaton.toDot());
		
		ConstraintToAutomaton instance = new ConstraintToAutomaton(eventsList);
		
		List<List<String>> sequences = new ArrayList<List<String>>();
		for (int i=0; i<seqStr.length; i++) {
			List<String> sequence = new ArrayList<String>();
			for (int j=0; j<seqStr[i].length; j++) {
				sequence.add(seqStr[i][j]);
			}
			sequences.add(sequence);
		}
		
		int i = 0;
		boolean[] actuals = new boolean[sequences.size()];
		for (List<String> seq : sequences) {
			boolean isValid = intersectionAutomaton.run(instance.mappingSequenceToCharArray(seq));
			actuals[i] = isValid;
			
			System.out.println("Seq #" + i + " " + seq + " validity is " + isValid);
			i++;
		}
		System.out.println();
		
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testIN1() {
		//motivating example
		String[] events = {"a", "b", "c"};
		
		List<String> constraints = new ArrayList<String>();
		String expr;
		
		expr = "a ~ c";
		constraints.add(expr);
		
		/* Printed Result should be:
		Seq #0 [c] validity is true
		Seq #1 [a, a, b] validity is true
		Seq #2 [b, b, c] validity is true
		Seq #3 [a, a, b, c] validity is true
		*/
		String[][] seqStr = {
				{"c"}, {"a", "a", "b"}, {"b", "b", "c"}, 
				{"a", "a", "b", "c"}
		};
		boolean[] expecteds = {true, true, true, true};
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
		
		int count = 0;
		Automaton intersectionAutomaton = null;
		for (int i=0; i<constraints.size(); i++){
			String constraint = constraints.get(i);
			
			System.out.println("Sequencing Constraint #" + i + ": ");
			System.out.println("  " + constraint);
			
			ConstraintParser parser = new ConstraintParser(constraint, eventsList);
			
			try {
				TypeInfo ti = parser.parse();
				
				Constraint sequencingConstraint = ti.getConstraint();
				
				ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
				Automaton automaton = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
				
				System.out.println(automaton.toDot());
				
				if (count == 0)
					intersectionAutomaton = automaton;
				else
					intersectionAutomaton = intersectionAutomaton.intersection(automaton);
				
				count++;
				
			} catch (Exception ex) {
				System.out.print(ex);
			}
		}
		
		System.out.println("Intersection Automaton is : ");
		System.out.println(intersectionAutomaton.toDot());
		
		ConstraintToAutomaton instance = new ConstraintToAutomaton(eventsList);
		
		List<List<String>> sequences = new ArrayList<List<String>>();
		for (int i=0; i<seqStr.length; i++) {
			List<String> sequence = new ArrayList<String>();
			for (int j=0; j<seqStr[i].length; j++) {
				sequence.add(seqStr[i][j]);
			}
			sequences.add(sequence);
		}
		
		int i = 0;
		boolean[] actuals = new boolean[sequences.size()];
		for (List<String> seq : sequences) {
			boolean isValid = intersectionAutomaton.run(instance.mappingSequenceToCharArray(seq));
			actuals[i] = isValid;
			
			System.out.println("Seq #" + i + " " + seq + " validity is " + isValid);
			i++;
		}
		System.out.println();
		
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testConcat1() {
		//motivating example
		String[] events = {"a", "b", "c"};
		
		List<String> constraints = new ArrayList<String>();
		String expr;
		
		expr = "(a.a.a) ...+ b";
		constraints.add(expr);
		
		/* Printed Result should be:
		Seq #0 [b] validity is false
		Seq #1 [a, a, b] validity is false
		Seq #2 [a, c, a, a, b] validity is false
		Seq #3 [a] validity is true
		Seq #4 [a, a] validity is true
		Seq #5 [a, a, a] validity is true
		Seq #6 [a, a, a, b] validity is true
		Seq #7 [a, a, a, a, b] validity is true
		Seq #8 [a, a, a, c, b] validity is true
		*/
		String[][] seqStr = {
				{"b"}, {"a", "a", "b"}, {"a", "c", "a", "a", "b"}, 
				{"a"}, {"a", "a"}, {"a", "a", "a"},
				{"a", "a", "a", "b"}, {"a", "a", "a", "a", "b"}, {"a", "a", "a", "c", "b"}
		};
		boolean[] expecteds = {false, false, false, 
				true, true, true,
				true, true, true};
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
		
		int count = 0;
		Automaton intersectionAutomaton = null;
		for (int i=0; i<constraints.size(); i++){
			String constraint = constraints.get(i);
			
			System.out.println("Sequencing Constraint #" + i + ": ");
			System.out.println("  " + constraint);
			
			ConstraintParser parser = new ConstraintParser(constraint, eventsList);
			
			try {
				TypeInfo ti = parser.parse();
				
				Constraint sequencingConstraint = ti.getConstraint();
				
				ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
				Automaton automaton = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
				
				System.out.println(automaton.toDot());
				
				if (count == 0)
					intersectionAutomaton = automaton;
				else
					intersectionAutomaton = intersectionAutomaton.intersection(automaton);
				
				count++;
				
			} catch (Exception ex) {
				System.out.print(ex);
			}
		}
		
		System.out.println("Intersection Automaton is : ");
		System.out.println(intersectionAutomaton.toDot());
		
		ConstraintToAutomaton instance = new ConstraintToAutomaton(eventsList);
		
		List<List<String>> sequences = new ArrayList<List<String>>();
		for (int i=0; i<seqStr.length; i++) {
			List<String> sequence = new ArrayList<String>();
			for (int j=0; j<seqStr[i].length; j++) {
				sequence.add(seqStr[i][j]);
			}
			sequences.add(sequence);
		}
		
		int i = 0;
		boolean[] actuals = new boolean[sequences.size()];
		for (List<String> seq : sequences) {
			boolean isValid = intersectionAutomaton.run(instance.mappingSequenceToCharArray(seq));
			actuals[i] = isValid;
			
			System.out.println("Seq #" + i + " " + seq + " validity is " + isValid);
			i++;
		}
		System.out.println();
		
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testBasicAutomaton() {
		//motivating example
		String[] events = {"a", "b", "c", "d"};
		
		String[] constraints = {
				"{a, b}",
				"_ {a, b}",
				"{a, b} +- {b, c}",
				"{a, b} +... {b, c}",
				"{a, b} -+ {b, c}",
				"{a, b} ...+ {b, c}",
				"{a, b} ~ {b, c}",
				"{a, b} .~. {b, c}",
		};
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
		
		for (String constraint : constraints) {
			System.out.println(constraint);
					
			ConstraintParser parser = new ConstraintParser(constraint, eventsList);
			Automaton automaton = null;
			try {
				TypeInfo ti = parser.parse();
				
				Constraint sequencingConstraint = ti.getConstraint();
				
				ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
				automaton = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
				
				System.out.println(automaton.toDot());	
			} catch (Exception ex) {
				System.out.print(ex);
			}
		}
		
	}
	
	@Test
	public void testNestingAutomaton1() {
		//motivating example
		String[] events = {"a", "b", "c"};
		
		String[] constraints = {
				"(_ a && {b, c} +... a && {a, b} .~. a)", // equals to RE "c*a"
				"(c +... a && (_ c && c .~. a) ~ c && (_ c && c .~. a) -+ a)", // equals to RE "(b*(cb*a)*)*"
		};
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
		
		for (String constraint : constraints) {
			System.out.println(constraint);
					
			ConstraintParser parser = new ConstraintParser(constraint, eventsList);
			Automaton automaton = null;
			try {
				TypeInfo ti = parser.parse();
				
				Constraint sequencingConstraint = ti.getConstraint();
				
				ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
				automaton = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
				
				System.out.println(automaton.toDot());	
			} catch (Exception ex) {
				System.out.print(ex);
			}
		}
		
	}
	
	@Test
	public void testNestingAutomaton2() {
		//motivating example
		String[] events = {"Open", "Read", "Write", "Close"};
		
		String[] constraints = {
			
		};
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
		
		for (String constraint : constraints) {
			System.out.println(constraint);
					
			ConstraintParser parser = new ConstraintParser(constraint, eventsList);
			Automaton automaton = null;
			try {
				TypeInfo ti = parser.parse();
				
				Constraint sequencingConstraint = ti.getConstraint();
				
				ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
				automaton = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
				
				System.out.println(automaton.toDot());	
			} catch (Exception ex) {
				System.out.print(ex);
			}
		}
		
	}
	
	@Test
	public void testNestingAutomaton3() {
		//motivating example
		String[] events = {"req", "abt", "rsp", "others"};
		
		List<String> constraints = new ArrayList<String>();
		String expr;
		
		expr = "(_ req && req ~ _ {abt, rsp}) -+ {abt, rsp}"
				+ " && req +- (_ {abt, rsp} && _ req ~ {abt, rsp})"
				+ "";
		constraints.add(expr);
		
		/* Printed Result should be:
		Seq #0 [others] validity is true
		Seq #1 [req, others, rsp] validity is true
		Seq #2 [req, others, abt] validity is true
		Seq #3 [req, abt, req, others, rsp] validity is true
		Seq #4 [others, abt, req, rsp] validity is false
		Seq #5 [req, abt, rsp] validity is false
		Seq #6 [req, req, rsp] validity is false
		Seq #7 [others, abt, req, rsp, rsp] validity is false
		Seq #8 [others, req, others] validity is false
		*/
		String[][] seqStr = {
				{"others"}, {"req", "others", "rsp"}, {"req", "others", "abt"}, 
				{"req", "abt", "req", "others", "rsp"}, 
				{"others", "abt", "req", "rsp"},
				{"req", "abt", "rsp"}, 
				{"req", "req", "rsp"},
				{"others", "abt", "req", "rsp", "rsp"},
				{"others", "req", "others"},
		};
		boolean[] expecteds = {true, true, true, true, 
				false, false, false, false, false};
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
		
		int count = 0;
		Automaton intersectionAutomaton = null;
		for (int i=0; i<constraints.size(); i++){
			String constraint = constraints.get(i);
			
			System.out.println("Sequencing Constraint #" + i + ": ");
			System.out.println("  " + constraint);
			
			ConstraintParser parser = new ConstraintParser(constraint, eventsList);
			
			try {
				TypeInfo ti = parser.parse();
				
				Constraint sequencingConstraint = ti.getConstraint();
				
				ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
				Automaton automaton = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
				
				System.out.println(automaton.toDot());
				
				if (count == 0)
					intersectionAutomaton = automaton;
				else
					intersectionAutomaton = intersectionAutomaton.intersection(automaton);
				
				count++;
				
			} catch (Exception ex) {
				System.out.print(ex);
			}
		}
		
		System.out.println("Intersection Automaton is : ");
		System.out.println(intersectionAutomaton.toDot());
		
		ConstraintToAutomaton instance = new ConstraintToAutomaton(eventsList);
		
		List<List<String>> sequences = new ArrayList<List<String>>();
		for (int i=0; i<seqStr.length; i++) {
			List<String> sequence = new ArrayList<String>();
			for (int j=0; j<seqStr[i].length; j++) {
				sequence.add(seqStr[i][j]);
			}
			sequences.add(sequence);
		}
		
		int i = 0;
		boolean[] actuals = new boolean[sequences.size()];
		for (List<String> seq : sequences) {
			boolean isValid = intersectionAutomaton.run(instance.mappingSequenceToCharArray(seq));
			actuals[i] = isValid;
			
			System.out.println("Seq #" + i + " " + seq + " validity is " + isValid);
			i++;
		}
		System.out.println();
		
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testModelFileIO() {
		//motivating example {Open, Read, Write, Close}
		String[] events = {"O", "R", "W", "C"};
		
		List<String> constraints = new ArrayList<String>();
		String expr;
		
		expr = "O ...+ {C, R, W}";
		constraints.add(expr);
		
		expr = "(_ O && O .~. C) -+ {R, W, C}"; // File must be Open before Read/Write/Close
		constraints.add(expr);
		
		expr = "{O, R, W} +... C";
		constraints.add(expr);
		
		/* Printed Result should be:
		Seq #0 [O] validity is false
		Seq #1 [O, C] validity is true
		Seq #2 [O, C, O] validity is false
		Seq #3 [O, O, C] validity is true
		Seq #4 [O, R, W, C] validity is true
		Seq #5 [O, C, O, C] validity is true
		Seq #6 [O, C, O, R, W, C, O, C] validity is true
		Seq #7 [O, W, O, R, R, W, C, C] validity is false
		*/
		String[][] seqStr = {
				{"O"},
				{"O", "C"},
				{"O", "C", "O"},
				{"O", "O", "C"},
				{"O", "R", "W", "C"},
				{"O", "C", "O", "C"},
				{"O", "C", "O", "R", "W", "C", "O", "C"},
				{"O", "W", "O", "R", "R", "W", "C", "C"},
		};
		boolean[] expectedsValid = {false, true, false, true, true, true, true, false};
		/* Printed Result should be:
		Seq #0 [O] extensibility is true
		Seq #1 [O, C] extensibility is true
		Seq #2 [O, C, O] extensibility is true
		Seq #3 [O, O, C] extensibility is false
		Seq #4 [O, R, W, C] extensibility is true
		Seq #5 [O, C, O, C] extensibility is false
		Seq #6 [O, C, O, R, W, C, O, C] extensibility is false
		Seq #7 [O, W, O, R, R, W, C, C] extensibility is false
		*/
		boolean[] expectedsExtend = {true, true, true, false, true, false, false, false};
		
		int t = 2;
		int maxRepetition = 2;
		int maxLength = 8;
		// motivating example end
		
		Subsequences subseqs = new Subsequences(events, t, maxRepetition);
		System.out.println(subseqs.getListOfEvents().size() + " Events: ");
		System.out.println("  " + subseqs.getListOfEvents());
		
		System.out.println("# of " + t + "-way permutations (with repetition <=" + maxRepetition + ") =  " + subseqs.getSetOfTwaySubseqs().size());
		System.out.println("  " + subseqs.getSetOfTwaySubseqs());
		
		System.out.println("Repetition Constraints: ");
		System.out.println("  " + "0 <= Event.# <= " + maxRepetition);
		
		System.out.println("Length Constraints: ");
		System.out.println("  " +  "1 <= Len <= " + maxLength);
		
		List<String> eventsList = subseqs.getListOfEvents();
		
		int count = 0;
		Automaton intersectionAutomaton = null;
		for (int i=0; i<constraints.size(); i++){
			String constraint = constraints.get(i);
			
			System.out.println("Sequencing Constraint #" + i + ": ");
			System.out.println("  " + constraint);
			
			ConstraintParser parser = new ConstraintParser(constraint, eventsList);
			
			try {
				TypeInfo ti = parser.parse();
				
				Constraint sequencingConstraint = ti.getConstraint();
				
				ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
				Automaton automaton = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
				
				System.out.println(automaton.toDot());
				
				if (count == 0)
					intersectionAutomaton = automaton;
				else
					intersectionAutomaton = intersectionAutomaton.intersection(automaton);
				
				count++;
				
			} catch (Exception ex) {
				System.out.print(ex);
			}
		}
		
		System.out.println("Intersection Automaton is : ");
		System.out.println(intersectionAutomaton.toDot());
		
		ConstraintToAutomaton instance = new ConstraintToAutomaton(eventsList);
		
		List<List<String>> sequences = new ArrayList<List<String>>();
		
		for (int i=0; i<seqStr.length; i++) {
			List<String> sequence = new ArrayList<String>();
			for (int j=0; j<seqStr[i].length; j++) {
				sequence.add(seqStr[i][j]);
			}
			sequences.add(sequence);
		}
		
		int i = 0;
		boolean[] actualsValid = new boolean[sequences.size()];
		for (List<String> seq : sequences) {
			boolean isValid = intersectionAutomaton.run(instance.mappingSequenceToCharArray(seq));
			actualsValid[i] = isValid;
			
			System.out.println("Seq #" + i + " " + seq + " validity is " + isValid);
			i++;
		}
		System.out.println();
		
		assertArrayEquals(expectedsValid, actualsValid);
		
		i = 0;
		boolean[] actualsExtend = new boolean[sequences.size()];
		for (List<String> seq : sequences) {
			boolean isExtendable = instance.isExtendableinDFS(seq, maxLength, maxRepetition, intersectionAutomaton);
			actualsExtend[i] = isExtendable;
			
			System.out.println("Seq #" + i + " " + seq + " extensibility is " + isExtendable);
			
			i++;
		}
		System.out.println();
		
		assertArrayEquals(expectedsExtend, actualsExtend);
	}
	
	@Test
	public void testEquvalienceOfCons1() {
		//motivating example
		
		String[] events = {"a", "b", "c", "d"};
		
		// Assert that "{a, b} +- (_ {b, c})" equals to "{a, b} +... {b, c}"
		String constraint1 = "{a, b} +- (_ {b, c})";
		String constraint2 = "{a, b} +... {b, c}";
		
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
			
		ConstraintParser parser = new ConstraintParser(constraint1, eventsList);
		Automaton automaton1 = null;
		try {
			TypeInfo ti = parser.parse();
			
			Constraint sequencingConstraint = ti.getConstraint();
			
			ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
			automaton1 = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
			
			System.out.println(automaton1.toDot());	
		} catch (Exception ex) {
			System.out.print(ex);
		}
		
		parser = new ConstraintParser(constraint2, eventsList);
		Automaton automaton2 = null;
		try {
			TypeInfo ti = parser.parse();
			
			Constraint sequencingConstraint = ti.getConstraint();
			
			ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
			automaton2 = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
			
			System.out.println(automaton2.toDot());	
		} catch (Exception ex) {
			System.out.print(ex);
		}
		
		assertTrue(automaton1.equals(automaton2));
	}
	
	@Test
	public void testEquvalienceOfCons2() {
		//motivating example
		String[] events = {"a", "b", "c", "d"};
		
		// Assert that "(_ {a, b}) -+ {b, c}" equals to "{a, b} ...+ {b, c}"
		String constraint1 = "(_ {a, b}) -+ {b, c}";
		String constraint2 = "{a, b} ...+ {b, c}";
		
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
			
		ConstraintParser parser = new ConstraintParser(constraint1, eventsList);
		Automaton automaton1 = null;
		try {
			TypeInfo ti = parser.parse();
			
			Constraint sequencingConstraint = ti.getConstraint();
			
			ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
			automaton1 = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
			
			System.out.println(automaton1.toDot());	
		} catch (Exception ex) {
			System.out.print(ex);
		}
		
		parser = new ConstraintParser(constraint2, eventsList);
		Automaton automaton2 = null;
		try {
			TypeInfo ti = parser.parse();
			
			Constraint sequencingConstraint = ti.getConstraint();
			
			ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
			automaton2 = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
			
			System.out.println(automaton2.toDot());	
		} catch (Exception ex) {
			System.out.print(ex);
		}
		
		assertTrue(automaton1.equals(automaton2));
	}
	
	@Test
	public void testEquvalienceOfCons3() {
		//motivating example
		String[] events = {"a", "b", "c", "d"};
		
		// Assert that "(_ {a, b}) ~ {b, c}" equals to "{a, b} .~. {b, c}"
		String constraint1 = "(_ {a, b}) ~ {b, c}";
		String constraint2 = "{a, b} .~. {b, c}";
		
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
			
		ConstraintParser parser = new ConstraintParser(constraint1, eventsList);
		Automaton automaton1 = null;
		try {
			TypeInfo ti = parser.parse();
			
			Constraint sequencingConstraint = ti.getConstraint();
			
			ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
			automaton1 = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
			
			System.out.println(automaton1.toDot());	
		} catch (Exception ex) {
			System.out.print(ex);
		}
		
		parser = new ConstraintParser(constraint2, eventsList);
		Automaton automaton2 = null;
		try {
			TypeInfo ti = parser.parse();
			
			Constraint sequencingConstraint = ti.getConstraint();
			
			ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
			automaton2 = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
			
			System.out.println(automaton2.toDot());	
		} catch (Exception ex) {
			System.out.print(ex);
		}
		
		assertTrue(automaton1.equals(automaton2));
	}
	
	@Test
	public void testEquvalienceOfCons4() {
		//motivating example
		String[] events = {"Open", "Read", "Write", "Close"};
		
		// Assert that "!_({Close}.{Read,Write,Close})" equals to "{Close} ~ {Read,Write,Close}"
		String constraint1 = "!_({Close}.{Read,Write,Close})";
		String constraint2 = "{Close} ~ {Read,Write,Close}";
		
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
			
		ConstraintParser parser = new ConstraintParser(constraint1, eventsList);
		Automaton automaton1 = null;
		try {
			TypeInfo ti = parser.parse();
			
			Constraint sequencingConstraint = ti.getConstraint();
			
			ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
			automaton1 = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
			
			System.out.println(automaton1.toDot());	
		} catch (Exception ex) {
			System.out.print(ex);
		}
		
		parser = new ConstraintParser(constraint2, eventsList);
		Automaton automaton2 = null;
		try {
			TypeInfo ti = parser.parse();
			
			Constraint sequencingConstraint = ti.getConstraint();
			
			ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
			automaton2 = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
			
			System.out.println(automaton2.toDot());	
		} catch (Exception ex) {
			System.out.print(ex);
		}
		
		assertTrue(automaton1.equals(automaton2));
	}
	
	@Test
	public void testEquvalienceOfNesting() {
		//motivating example
		
		String[] events = {"a", "b", "c", "d"};
		
		// Assert that "e1 +- {e2, e3}" = "e1 +- (e2 || e3)"
		String constraint1 = "a +- {b, c}";
		String constraint2 = "a +- (b || c)";
		
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
			
		ConstraintParser parser = new ConstraintParser(constraint1, eventsList);
		Automaton automaton1 = null;
		try {
			TypeInfo ti = parser.parse();
			
			Constraint sequencingConstraint = ti.getConstraint();
			
			ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
			automaton1 = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
			
			System.out.println(automaton1.toDot());	
		} catch (Exception ex) {
			System.out.print(ex);
		}
		
		parser = new ConstraintParser(constraint2, eventsList);
		Automaton automaton2 = null;
		try {
			TypeInfo ti = parser.parse();
			
			Constraint sequencingConstraint = ti.getConstraint();
			
			ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
			automaton2 = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
			
			System.out.println(automaton2.toDot());	
		} catch (Exception ex) {
			System.out.print(ex);
		}
		
		assertTrue(automaton1.equals(automaton2));
	}
	
	@Test
	public void testEquvalienceOfFileAPI() {
		//motivating example
		
		String[] events = {"open", "close", "read", "write"};
		
		// Assert that constraint1 = constraint2
		String constraint1 = "{open, read, write} +... close && open ...+ {read, write, close} && (_ open && open .~. close) -+ {read, write, close}";
		String constraint2 = "open +- _ close && (_ open && open .~. close) -+ {read, write, close}";
//		String constraint2 = "open +- _ close && (_ open && open .~. close) -+ {read, write, close} && (_ close && close ~ _ open) ~ {read, write, close}";
		
//		String constraint1 = "(_ open && open ~ _ close) -+ {read, write, close}";
//		String constraint2 = "(_ open && open ~ _ close) -+ {read, write, close} && (_ close && close ~ _ open) ~ {read, write, close}";
////		String constraint2 = "(_ close && close ~ _ open) ~ {read, write, close}";
		
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
			
		ConstraintParser parser = new ConstraintParser(constraint1, eventsList);
		Automaton automaton1 = null;
		try {
			TypeInfo ti = parser.parse();
			
			Constraint sequencingConstraint = ti.getConstraint();
			
			ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
			automaton1 = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
			
			System.out.println(automaton1.toDot());	
		} catch (Exception ex) {
			System.out.print(ex);
		}
		
		parser = new ConstraintParser(constraint2, eventsList);
		Automaton automaton2 = null;
		try {
			TypeInfo ti = parser.parse();
			
			Constraint sequencingConstraint = ti.getConstraint();
			
			ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
			automaton2 = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
			
			System.out.println(automaton2.toDot());	
		} catch (Exception ex) {
			System.out.print(ex);
		}
		
		assertTrue(automaton1.equals(automaton2));
	}
	
	@Test
	public void testEqualBtwConsAndAuto1() {
		//motivating example
		String[] events = {"req", "abt", "rsp", "others"};
		
		// "(_ req && req ~ _ {abt, rsp}) -+ {abt, rsp} && req +- (_ {abt, rsp} && _ req ~ {abt, rsp})"
		// equals to "(others*.(req.others*.{abt,rsp})*)*",
		// i.e., if e1^e2 is empty, 
		// "(_ e1 && e1 ~ _ e2) -+ e2 && e1 +- (_ e2 && _ e1 ~ e2)"
		// = "((Cu(e1Ve2))*.(e1.(Cu(e1Ve2))*.e2)*)*".
		// Notice that Cu is the complementary event set.
		// If hiding all events in Cu(e1Ve2) such as others event, 
		// then it is (e1.e2)* such as "(req.{abt,rsp})*".
		String constraint1 = "(_ req && req ~ _ {abt, rsp}) -+ {abt, rsp}"
				+ " && req +- (_ {abt, rsp} && _ req ~ {abt, rsp})"
				;
		
		// Create automaton as Regex "(d*(ad*(b|c))*)*"
		Automaton automaton2 = new Automaton();
		State s1 = new State();
		State s2 = new State();
		s1.setAccept(true);
		automaton2.setInitialState(s1);
		// s1 --d-> s1
		s1.addTransition(new Transition('d', s1));
		// s1 --a-> s2
		s1.addTransition(new Transition('a', s2));
		// s2 --d-> s2
		s2.addTransition(new Transition('d', s2));
		// s2 --b or c-> s1
		s2.addTransition(new Transition('b', s1));
		s2.addTransition(new Transition('c', s1));
		
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
			
		ConstraintParser parser = new ConstraintParser(constraint1, eventsList);
		Automaton automaton1 = null;
		try {
			TypeInfo ti = parser.parse();
			
			Constraint sequencingConstraint = ti.getConstraint();
			
			ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
			automaton1 = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
			
			System.out.println(automaton1.toDot());	
		} catch (Exception ex) {
			System.out.print(ex);
		}
		
		System.out.println("Comparing above automaton with below automaton:");
		System.out.println(automaton2.toDot());
		
		assertTrue(automaton1.equals(automaton2));
	}
	
	@Test
	public void testEqualBtwConsAndAuto2() {
		//motivating example
		String[] events = {"a", "b", "c", "d"};
		
		// "(_ e1 && e1 ~ _ e2) -+ e2 && e1 +- (_ e2 && _ e1 ~ e2)"
		// = "( (Cu(e1Ve2))* ((e1/e2)((e1^e2)|(Cu(e1Ve2))*)*(e2/e1))* )*".
		// Notice that Cu is the complementary event set.
		String constraint1 = "(_ {a, b} && {a, b} ~ _ {b, c}) -+ {b, c}"
				+ " && {a, b} +- (_ {b, c} && _ {a, b} ~ {b, c})"
				;
		
		// Create automaton as Regex "( d* (a(b|d)*c)* )*"
		Automaton automaton2 = new Automaton();
		State s1 = new State();
		State s2 = new State();
		s1.setAccept(true);
		automaton2.setInitialState(s1);
		// s1 --d-> s1
		s1.addTransition(new Transition('d', s1));
		// s1 --a-> s2
		s1.addTransition(new Transition('a', s2));
		// s2 --b or d-> s2
		s2.addTransition(new Transition('b', s2));
		s2.addTransition(new Transition('d', s2));
		// s2 --c-> s1
		s2.addTransition(new Transition('c', s1));
		
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
			
		ConstraintParser parser = new ConstraintParser(constraint1, eventsList);
		Automaton automaton1 = null;
		try {
			TypeInfo ti = parser.parse();
			
			Constraint sequencingConstraint = ti.getConstraint();
			
			ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
			automaton1 = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
			
			System.out.println(automaton1.toDot());	
		} catch (Exception ex) {
			System.out.print(ex);
		}
		
		System.out.println("Comparing above automaton with below automaton:");
		System.out.println(automaton2.toDot());
		
		assertTrue(automaton1.equals(automaton2));
	}
	
	
	@Test
	public void testEqualBtwConsAndAutoOfPHDManager() {
		// case study start
		// Use the labels in Mealy Machine (input/output) as events:
		// the whole set of events = [RxAssocReq_unacceptable_configuration/TxAssocRsp_rejected, RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config, RxConfigEventReportReq, REQAgentSuppliedUnsupportedConfig/TxConfigEventReportRsp_unsupported_config, REQAgentSuppliedSupportedConfig/TxConfigEventReportRsp_accepted_config, RxAssocRelReq/TxAssocRelRsp, REQAssocRel/TxAssocRelReq, REQAssocRel, RxAssocRelRsp]
		String[] events = {
			"RxAssocReq_unacceptable_configuration/TxAssocRsp_rejected", 
			"RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted", 
			"RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config", 
			
			"RxConfigEventReportReq",
			"REQAgentSuppliedUnsupportedConfig/TxConfigEventReportRsp_unsupported_config", 
			"REQAgentSuppliedSupportedConfig/TxConfigEventReportRsp_accepted_config", 
			
			"RxAssocRelReq/TxAssocRelRsp", 
			
			"REQAssocRel/TxAssocRelReq", 
			"REQAssocRel", 
			"RxAssocRelRsp"
			};
		
		String[] constraints = {
//			"{RxAssocReq_unacceptable_configuration/TxAssocRsp_rejected, RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config}...+ {RxConfigEventReportReq, REQAgentSuppliedUnsupportedConfig/TxConfigEventReportRsp_unsupported_config, REQAgentSuppliedSupportedConfig/TxConfigEventReportRsp_accepted_config, RxAssocRelReq/TxAssocRelRsp, REQAssocRel/TxAssocRelReq, REQAssocRel, RxAssocRelRsp}",
			"{RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config, RxConfigEventReportReq, REQAgentSuppliedUnsupportedConfig/TxConfigEventReportRsp_unsupported_config, REQAgentSuppliedSupportedConfig/TxConfigEventReportRsp_accepted_config, REQAssocRel/TxAssocRelReq, REQAssocRel} +... {RxAssocRelReq/TxAssocRelRsp, RxAssocRelRsp, RxAssocReq_unacceptable_configuration/TxAssocRsp_rejected}",
			
			"(_ {RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config, REQAgentSuppliedUnsupportedConfig/TxConfigEventReportRsp_unsupported_config} && {RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config, REQAgentSuppliedUnsupportedConfig/TxConfigEventReportRsp_unsupported_config} .~. {RxConfigEventReportReq, RxAssocRelReq/TxAssocRelRsp, REQAssocRel/TxAssocRelReq}) -+ RxConfigEventReportReq",
			"RxConfigEventReportReq -+ {REQAgentSuppliedUnsupportedConfig/TxConfigEventReportRsp_unsupported_config, REQAgentSuppliedSupportedConfig/TxConfigEventReportRsp_accepted_config}",
			
			// "REQAssocRel/TxAssocRelReq ... RxAssocRelRsp", h ... j = (_ h && h .~. j) -+ j && h +- (_ j && h .~. j)
			"(_ REQAssocRel/TxAssocRelReq && REQAssocRel/TxAssocRelReq .~. RxAssocRelRsp) -+ RxAssocRelRsp && REQAssocRel/TxAssocRelReq +- (_ RxAssocRelRsp && REQAssocRel/TxAssocRelReq .~. RxAssocRelRsp)",
			
			"(_ {RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config} && {RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config} .~. {RxAssocRelReq/TxAssocRelRsp, REQAssocRel/TxAssocRelReq}) -+ REQAssocRel/TxAssocRelReq",
			"((_ {RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config} && {RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config} .~. {RxAssocRelReq/TxAssocRelRsp, REQAssocRel/TxAssocRelReq}) || (_ REQAssocRel/TxAssocRelReq && REQAssocRel/TxAssocRelReq .~. RxAssocRelRsp)) -+ RxAssocRelReq/TxAssocRelRsp",
			"(_ REQAssocRel/TxAssocRelReq && REQAssocRel/TxAssocRelReq .~. RxAssocRelRsp) -+ REQAssocRel",
			"((_ {RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config} && {RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config} .~. {RxAssocRelReq/TxAssocRelRsp, REQAssocRel/TxAssocRelReq}) || (_ REQAssocRel/TxAssocRelReq && REQAssocRel/TxAssocRelReq .~. RxAssocRelRsp)) ~ {RxAssocReq_unacceptable_configuration/TxAssocRsp_rejected, RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config}",
		};
		
		/* sequencing constraints */
		// Events Mapping:
		// [RxAssocReq_unacceptable_configuration/TxAssocRsp_rejected, RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config, RxConfigEventReportReq, REQAgentSuppliedUnsupportedConfig/TxConfigEventReportRsp_unsupported_config, REQAgentSuppliedSupportedConfig/TxConfigEventReportRsp_accepted_config, RxAssocRelReq/TxAssocRelRsp, REQAssocRel/TxAssocRelReq, REQAssocRel, RxAssocRelRsp]
		// [a, b, c, d, e, f, g, h, i, j]
		// a RxAssocReq_unacceptable_configuration/TxAssocRsp_rejected, 
		// b RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, 
		// c RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config, 
		
		// d RxConfigEventReportReq, 
		// e REQAgentSuppliedUnsupportedConfig/TxConfigEventReportRsp_unsupported_config, 
		// f REQAgentSuppliedSupportedConfig/TxConfigEventReportRsp_accepted_config, 
		
		// g RxAssocRelReq/TxAssocRelRsp, 
		
		// h REQAssocRel/TxAssocRelReq, 
		// i REQAssocRel, 
		// j RxAssocRelRsp
//		String[] events = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};
//		
//		String[] constraints = {
////			"{a, b, c} ...+ {d, e, f, g, h, i, j}", // Cons 1, the possible first events, is included by Cons 10
//			"{b, c, d, e, f, h, i} +... {a, g, j}", // 2, the possible last events
//			
//			// corrected 9 + corrected 5, the relationships of events about config
//			"(_ {c, e} && {c, e} .~. {d, g, h}) -+ d",
//			"d -+ {e, f}", // notice that "+-" is not applicable here since events g, h are also possible to happen immediately after d
////			"d +- {e, f, g, h}", // which is correct but redundant with other cons
//			
//			"(_ h && h .~. j) -+ j && h +- (_ j && h .~. j)", // corrected 6, TxReq corresponds to RxRsp with same device-id, can abbr to "h ... j"
//			"(_ {b, c} && {b, c} .~. {g, h}) -+ h", // updated 7-1, REQAssocRel/TxAssocRelReq can only happen when associated
//			"((_ {b, c} && {b, c} .~. {g, h}) || (_ h && h .~. j)) -+ g", // updated 7-2, RxAssocRelReq/TxAssocRelRsp can only happen when Associated or Disassociating
//			"(_ h && h .~. j) -+ i", // corrected 8, when Disassociating, ignore more REQAssocRel as no output
//			
//			// Add a constraint "Rx_assoc_req can only happen when State is Unassociated",
//			// in other words, "Rx_assoc_req never happen when State is not Unassociated".
//			"((_ {b, c} && {b, c} .~. {g, h}) || (_ h && h .~. j)) ~ {a, b, c}" // 10
//			};
		
		String constraint1 = "";
		if (constraints.length >= 1)
		{
			constraint1 = constraints[0];
			for (int i=1; i<constraints.length; i++) {
				constraint1 += " && " + constraints[i];
			}
		}
				
		// Create automaton of antidote manager from IEEE11073 Whitepaper
		// Events Mapping:
		// [RxAssocReq_unacceptable_configuration/TxAssocRsp_rejected, RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config, RxConfigEventReportReq, REQAgentSuppliedUnsupportedConfig/TxConfigEventReportRsp_unsupported_config, REQAgentSuppliedSupportedConfig/TxConfigEventReportRsp_accepted_config, RxAssocRelReq/TxAssocRelRsp, REQAssocRel/TxAssocRelReq, REQAssocRel, RxAssocRelRsp]
		// [a, b, c, d, e, f, g, h, i, j]
		// a RxAssocReq_unacceptable_configuration/TxAssocRsp_rejected, 
		// b RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, 
		// c RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config, 
		// d RxConfigEventReportReq, 
		// e REQAgentSuppliedUnsupportedConfig/TxConfigEventReportRsp_unsupported_config, 
		// f REQAgentSuppliedSupportedConfig/TxConfigEventReportRsp_accepted_config, 
		// g RxAssocRelReq/TxAssocRelRsp, 
		// h REQAssocRel/TxAssocRelReq, 
		// i REQAssocRel, 
		// j RxAssocRelRsp
		Automaton automaton2 = new Automaton();
		State s1 = new State();
		State s2 = new State();
		State s3 = new State();
		State s4 = new State();
		State s5 = new State();
		s1.setAccept(true);
		automaton2.setInitialState(s1);
		// s1 --a-> s1
		s1.addTransition(new Transition('a', s1));
		// s1 --c-> s2
		s1.addTransition(new Transition('c', s2));
		// s1 --b-> s4
		s1.addTransition(new Transition('b', s4));
		
		// s2 --g-> s1
		s2.addTransition(new Transition('g', s1));
		// s2 --d-> s3
		s2.addTransition(new Transition('d', s3));
		// s2 --h-> s5
		s2.addTransition(new Transition('h', s5));
		
		// s3 --g-> s1
		s3.addTransition(new Transition('g', s1));
		// s3 --e-> s2
		s3.addTransition(new Transition('e', s2));
		// s3 --f-> s4
		s3.addTransition(new Transition('f', s4));
		// s3 --h-> s5
		s3.addTransition(new Transition('h', s5));
		
		// s4 --g-> s1
		s4.addTransition(new Transition('g', s1));
		// s4 --h-> s5
		s4.addTransition(new Transition('h', s5));
		
		// s5 --j-> s1
		s5.addTransition(new Transition('j', s1));
		// s5 --g or i-> s5
		s5.addTransition(new Transition('g', s5));
		s5.addTransition(new Transition('i', s5));
		
		// case study end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
			
		ConstraintParser parser = new ConstraintParser(constraint1, eventsList);
		Automaton automaton1 = null;
		try {
			TypeInfo ti = parser.parse();
			
			Constraint sequencingConstraint = ti.getConstraint();
			
			ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
			automaton1 = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
			
			System.out.println(automaton1.toDot());	
		} catch (Exception ex) {
			System.out.print(ex);
		}
		
		System.out.println("Comparing above automaton with below automaton:");
		System.out.println(automaton2.toDot());
		
		assertTrue(automaton1.equals(automaton2));
	}
	
	// By this test case, we found that parser cuts off the tail which it cannot parse,
	// such as "(_ a && a .~. b) -+ b) && a -+ c" equals to "(_ a && a .~. b) -+ b".
	// Thus, do double-check the writing of each constraint, especially before merge a lot of constraints by " && ". 
	@Test
	public void testParserCutOffTail() {
		//motivating example
		
		String[] events = {"a", "b", "c", "d"};
		
//		// Assert that "(_ a && a .~. b) -+ b)" equals to "(_ a && a .~. b) -+ b"
//		// Expected: True
//		String constraint1 = "(_ a && a .~. b) -+ b)";
//		String constraint2 = "(_ a && a .~. b) -+ b";
				
		// Assert that "(_ a && a .~. b) -+ b) && a -+ c" equals to "(_ a && a .~. b) -+ b"
		// Expected: True
		String constraint1 = "(_ a && a .~. b) -+ b) && a -+ c";
		String constraint2 = "(_ a && a .~. b) -+ b";
		
		// motivating example end
		
		List<String> eventsList = new ArrayList<String>();
		for (String event : events)
			eventsList.add(event);
			
		ConstraintParser parser = new ConstraintParser(constraint1, eventsList);
		Automaton automaton1 = null;
		try {
			TypeInfo ti = parser.parse();
			
			Constraint sequencingConstraint = ti.getConstraint();
			
			ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
			automaton1 = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
			
			System.out.println(automaton1.toDot());	
		} catch (Exception ex) {
			System.out.print(ex);
		}
		
		parser = new ConstraintParser(constraint2, eventsList);
		Automaton automaton2 = null;
		try {
			TypeInfo ti = parser.parse();
			
			Constraint sequencingConstraint = ti.getConstraint();
			
			ConstraintToAutomaton convertor = new ConstraintToAutomaton(eventsList);
			automaton2 = convertor.convertSequencingExprToAutomaton(sequencingConstraint);
			
			System.out.println(automaton2.toDot());	
		} catch (Exception ex) {
			System.out.print(ex);
		}
		
		assertTrue(automaton1.equals(automaton2));
	}

}
