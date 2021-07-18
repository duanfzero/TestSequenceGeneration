package generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import sequencingConstraints.ConstraintToAutomaton;
import sequencingConstraints.SolverByAutomaton;

/**
 * test sequence generator using the algorithm in my dissertation
 * 
 * @author Feng Duan
 *
 */
public class SequenceGeneratorByAutomaton {
	boolean isDebugMode = false;
	
	private List<String> listOfEvents;
	private Set<List<String>> setOfUncoveredTargetSeqs;
	private SolverByAutomaton solver;

	private Set<List<String>> setOfTestSequences;
	
	private Queue<List<String>> backupQueueOfTwaySequence; // as starting sequence candidates in BFS
	
	private Map<List<String>, Boolean> extendabilityCache = new HashMap<List<String>, Boolean>();
	
	public SequenceGeneratorByAutomaton(List<String> eventsList, SolverByAutomaton csolver, Set<List<String>> uncoveredTargetSeqs) {
		listOfEvents = eventsList;
		solver = csolver;
		setOfUncoveredTargetSeqs = uncoveredTargetSeqs;
		
		setOfTestSequences = new LinkedHashSet<List<String>>();
		
		// add t-way permutations with repetition as starting sequences candidates in BFS
		backupQueueOfTwaySequence = new LinkedList<List<String>>(setOfUncoveredTargetSeqs);
	}
	
	public List<String> startingPhase(Set<List<String>> uncoveredTargetSeqs){
		List<String> startingSequence = null;
		
		List<String> selectedTargetSeq = null;
		for (List<String> targetSeq : uncoveredTargetSeqs){
			if (isValid(targetSeq) || isExtendable(targetSeq)){
				selectedTargetSeq = targetSeq;
				break;
			}
		}
		
		if (selectedTargetSeq != null){
			setOfUncoveredTargetSeqs.remove(selectedTargetSeq);
			startingSequence = selectedTargetSeq;
			
			return startingSequence;
		}
		else {
			// create starting sequence from scratch, when target sequences are not valid nor extendable
			// note that only coverage > 0 should be returned, otherwise return null
			Set<List<String>> backupUncoveredTargetSeqs = new LinkedHashSet<List<String>>(setOfUncoveredTargetSeqs);
			
			startingSequence = new ArrayList<String>();
			
			// Create starting sequence to cover at least one uncovered target sequences
			// BFS + FirstMatch (Currently using in paper IWCT 2019)
			Queue<List<String>> queueOfTwaySequence = new LinkedList<List<String>>(backupQueueOfTwaySequence);
			startingSequence = genStartingSeqInBFS(uncoveredTargetSeqs, queueOfTwaySequence);
			
			if (startingSequence == null)
				return null;
			
			Set<List<String>> newlyCoveredSubseqs = new LinkedHashSet<List<String>>();
			for (List<String> subseq : backupUncoveredTargetSeqs){
				if (true == isCovered(startingSequence, subseq)){
					newlyCoveredSubseqs.add(subseq);
				}
			}
			
			int coverage = newlyCoveredSubseqs.size(); // it may cover no target
			if (coverage > 0) 
				return startingSequence;
			else
				return null;
		}
	}

	public void generate(){
		long genStartTime = System.currentTimeMillis();
		
		long startingPhaseCumulativeTime = 0;
		long extensionPhaseCumulativeTime = 0;
		
		// Generate sequences to cover all target sequences
		while (setOfUncoveredTargetSeqs.size() > 0) {
			/* Generate one sequence */
			
			debugPrintln("");
			debugPrintln("# of uncovered target sequences = " + setOfUncoveredTargetSeqs.size());
			debugPrintln(setOfUncoveredTargetSeqs.toString());
			
			// backup the set of uncovered target sequences to print newly covered target sequences at the end
			Set<List<String>> backupSetOfUncoveredTargetSeqs = new LinkedHashSet<List<String>>(setOfUncoveredTargetSeqs);
			
			long startingPhaseStartTime = System.currentTimeMillis();
			
			List<String> sequence = startingPhase(setOfUncoveredTargetSeqs);
			
			long startingPhaseEndTime = System.currentTimeMillis();
			long startingPhaseTime = startingPhaseEndTime - startingPhaseStartTime;
			debugPrintln("Starting Phase Time : " + (double)(startingPhaseTime)/1000 + " sec");
			
			startingPhaseCumulativeTime += startingPhaseTime;
			
			if (sequence == null || sequence.isEmpty()) {
				// If startingTestSequence cannot be created,
				// then all remaining uncovered target sequences are uncoverable.
				break;
			}
			
			// extension from starting test sequence 
			// (greedy for maximum coverage, loop ahead 1 window size for break-tie)
			long extensionPhaseStartTime = System.currentTimeMillis();
			
			int lookAheadWindowSize = 1; // by default = 1, window size for break-tie
			sequence = extensionPhase(setOfUncoveredTargetSeqs, sequence, lookAheadWindowSize); // returned sequence is complete: not-extendable but valid
			
			long extensionPhaseEndTime = System.currentTimeMillis();
			long extensionPhaseTime = extensionPhaseEndTime - extensionPhaseStartTime;
			debugPrintln("Extension Phase Time : " + (double)(extensionPhaseTime)/1000 + " sec");
			
			extensionPhaseCumulativeTime += extensionPhaseTime;
			
			setOfTestSequences.add(sequence);
			
			debugPrintln("Generated Sequence : ");
			debugPrintln(sequence.toString());
			
			backupSetOfUncoveredTargetSeqs.removeAll(setOfUncoveredTargetSeqs);
			debugPrintln("Covered target sequences : ");
			debugPrintln(backupSetOfUncoveredTargetSeqs.toString());
			
		}
		
		System.out.println("");
		System.out.println("Generation is Finished.");
		
		if (setOfUncoveredTargetSeqs.size() > 0) {
			System.out.println(setOfUncoveredTargetSeqs.size() + " Targets are uncoverable, under the three types of constraints!");
			System.out.println("  Unable to cover target sequences : " + setOfUncoveredTargetSeqs.toString());
		}
		else
			System.out.println("All targets are coverable!");
		
		long genEndTime = System.currentTimeMillis();
		long genElapsedTime = genEndTime - genStartTime;
		System.out.println("Elapsed Generation Time : " + (double)(genElapsedTime)/1000 + " sec " + 
				", it part of " + "Starting Phases Time : " +  (double)(startingPhaseCumulativeTime)/1000 + " sec " + 
				", and it part of " + "Extension Phases Time : " +  (double)(extensionPhaseCumulativeTime)/1000 + " sec ");
	}
	
	// if not found in cache, dispatch the extensibility check to DFS implementation (cache is good for extensibility check)
	private boolean isExtendable(List<String> ciSeq) {
		
		if (extendabilityCache.containsKey(ciSeq))
			return extendabilityCache.get(ciSeq);
		else {
			// Check via Automaton: 
			// Depth-First-Search from all possible ongoing edges of current state in Automaton, 
			// until an acceptable path is found
			boolean result = isExtendableViaAutomaton(ciSeq);
			extendabilityCache.put(ciSeq, result);
			
			return result;
		}
	}
	
	/**
	 * Recursively check if a CIS (Consecutive Input Sequence) is extendable via Automaton: 
	 * If any valid test sequence can be extended from it (append any possible event based on automaton), 
	 * or one of its extension is extendable, 
	 * then it is extendable.
	 * 
	 * @param ciSeq
	 * @return
	 */
	private boolean isExtendableViaAutomaton(List<String> ciSeq) {
		//  in extensibility check, seq can only reach maxLength - 1
		int maxLength = solver.getMAX_LENGTH();
		if (ciSeq.size() >= maxLength)
			return false;
		
		// Stop criterion as the Maximum Repetition of every event
		Map<String, Integer> eventCounter = new HashMap<String, Integer>();
		for (String event : ciSeq){
			if (!eventCounter.containsKey(event))
				eventCounter.put(event, 1);
			else {
				int count = eventCounter.get(event);
				eventCounter.put(event, count+1);
			}
		}
		for (Integer count : eventCounter.values()){
			if (count > solver.getMAX_REPETITION())
				return false;
		}
		
		List<String> eventCandidates = new ArrayList<String>();
		
		// Follow the automaton to extend
		Automaton automaton = solver.getAutomaton();
		ConstraintToAutomaton convertor = solver.getConvertor();
		// First, since the automaton is DFA, 
		// get the state that ciSeq arrives
		State currentState = automaton.getInitialState(); 
		for (String event : ciSeq) {
			char c = convertor.mappingEventToChar(event);
			currentState = currentState.step(c);
			
			if (currentState == null) {
//				System.out.println(ciSeq + " has no matching outgoing transition on " + automaton.toDot());
				return false;
			}
		}
		
		// Second, add all ongoing edges of this state into candidates
		for (Transition transition : currentState.getTransitions()) {
			char[] chars = new char[transition.getMax() - transition.getMin() + 1];
			int i = 0;
			for (char c = transition.getMin(); c <= transition.getMax(); c++)
				chars[i++] = c;
			
			eventCandidates.addAll(convertor.mappingCharArrayToSequence(chars));
		}
		
		for (int i = 0; i < eventCandidates.size(); i++){
			List<String> tempSequence = new ArrayList<String>(ciSeq);
			String event = eventCandidates.get(i);
			tempSequence.add(event);
			
			if (isValid(tempSequence)) {
//				System.out.println("Valid sequence during extensibility checking: " + tempSequence);
				return true;
			}
			else {
//				System.out.println("Invalid Sequence for extensibility checking: " + tempSequence);
				
				if (isExtendableViaAutomaton(tempSequence))
					return true;
			}
		}
		
		return false;
	}
	
	// dispatch the validity check to solver
	private boolean isValid(List<String> ciSeq) {
		return isValidOnConstraints(ciSeq);
	}
	
	/**
	 * check a CIS (Consecutive Input Sequence) is valid 
	 * if ciSeq satisfy all constraints and not exceed length limitation, then it is valid.  
	 * 
	 * @param ciSeq
	 * @return
	 */
	private boolean isValidOnConstraints(List<String> ciSeq) {
		//  in validity check, seq can reach maxLength
		int maxLength = solver.getMAX_LENGTH();
		if (ciSeq.size() > maxLength)
			return false;
		
		// Stop criterion as the Maximum Repetition of every event
		Map<String, Integer> eventCounter = new HashMap<String, Integer>();
		for (String event : ciSeq){
			if (!eventCounter.containsKey(event))
				eventCounter.put(event, 1);
			else {
				int count = eventCounter.get(event);
				eventCounter.put(event, count+1);
			}
		}
		for (Integer count : eventCounter.values()){
			if (count > solver.getMAX_REPETITION())
				return false;
		}
		
		// use SolverByAutomaton to check its validity.
		boolean isValidCCSeq = solver.solve(ciSeq);
			
		return isValidCCSeq;
	}
	
	/**
	 * In-Event-Order extension of sequence
	 * Greedy algorithm: compare the coverage of incremental extensions per event, polynomial complexity
	 * break-tie by looking ahead x window size, x = 1 by default
	 *	
	 * @param uncoveredSubseqs
	 * @param sequence
	 * @return
	 */
	private List<String> extensionPhase(Set<List<String>> uncoveredSubseqs, List<String> sequence, int lookAheadWindow) {
		int window = lookAheadWindow; // window size for break-tie on either maxCoverage > 0 or maxCoverage = 0
		if (window < 0)
			return sequence;
		
//		if (uncoveredSubseqs.size() == 0) // Bug: the last generated sequence may be in-complete if all subseqs are covered
//			return sequence;
		// Fix: only return when all subseqs are covered and this sequence is complete
		if (uncoveredSubseqs.size() == 0 && isValid(sequence))
			return sequence;
		
		// If the seq is extendable, meanings at least one path extending to complete test is found. 
		// Here we can place a cache mechanism to save time for the extensibility check of its extension.
		while (isExtendable(sequence)){
			int maxCoverage = -1; // there may happens that all extensions newlyCoverage = 0, but some of them can derive coverage in next event
			int maxIndex = -1;
			List<String> maxExtendedSequence = null;
			Set<List<String>> maxNewlyCoveredSubseqs = null;
			
			for (int i = 0; i < listOfEvents.size(); i++){
				List<String> extendedSequence = new ArrayList<String>(sequence);
				extendedSequence.add(listOfEvents.get(i));
				
				/* Only check new coverage with next event */
				// If the CISequence extended by new event is checked to be valid or extendable, 
				// then enumerate the number of newly covered target sequences (in-consecutive)
				Set<List<String>> newlyCoveredSubseqs = new LinkedHashSet<List<String>>();
				for (List<String> subseq : uncoveredSubseqs){
					if (true == isCovered(extendedSequence, subseq)){
						newlyCoveredSubseqs.add(subseq);
					}
				}
				
//				System.out.println(extendedSequence);
				
				int coverage = newlyCoveredSubseqs.size();
				if (coverage > maxCoverage) {
					// check valid and extendable after calculated coverage
					if (isValid(extendedSequence) || isExtendable(extendedSequence)){	
						maxCoverage = coverage;
						maxIndex = i; // store event index i for max coverage
						maxExtendedSequence = extendedSequence;
						maxNewlyCoveredSubseqs = newlyCoveredSubseqs;
					}
				}
				else if (coverage == maxCoverage) {
					/* TODO: break tie */
					// If two extended sequences have the same number,
					// then compare their next event coverage.
//					System.out.println("break tie");
				}
					
			}
			
			if (maxIndex > -1){
				// greedily choose the event that achieve local max coverage (maxCoverage > 0),
				// and continue to extend current CISequence with updated set of uncovered target sequences.
				uncoveredSubseqs.removeAll(maxNewlyCoveredSubseqs);
				
				sequence = maxExtendedSequence;
			}
			else{
				// max newly coverage = 0, but it is possible to cover new target by appending one more event
				sequence = maxExtendedSequence;
			}
		}

		return sequence;
	}
	
	/* Iterative BFS: search in level L and a node's L+1, for startingPhase to create starting test sequence (BFS)
	 * 
	 * FirstMatch: (1) L.cov > 0; or (2) L.cov = 0 && (L + 1).cov > 0 and so on.
	 * 
	 * @param uncoveredSubseqs
	 * @param sequence
	 * @return
	 */
	private List<String> genStartingSeqInBFS(Set<List<String>> uncoveredSubseqs, Queue<List<String>> queue) {
		
		while(!queue.isEmpty()){
			
			List<String> sequence = queue.poll();
				
			/* check coverage */
			Set<List<String>> newlyCoveredSubseqs = new LinkedHashSet<List<String>>();
			for (List<String> subseq : uncoveredSubseqs){
				if (true == isCovered(sequence, subseq)){
					newlyCoveredSubseqs.add(subseq);
				}
			}
			
	//		System.out.println(sequence);
				
			int coverage = newlyCoveredSubseqs.size();
			if (coverage > 0) {
				// check valid and extendable after calculated coverage
				if (isValid(sequence) || isExtendable(sequence)){
					uncoveredSubseqs.removeAll(newlyCoveredSubseqs);
					return sequence;
				}
			}
			else { // coverage = 0
				// all extendable sequences in level L are coverage = 0, so have to extend all extendable ones
				if (isExtendable(sequence)){
					for (int k=0; k<listOfEvents.size(); k++){
						List<String> extendedSequence = new ArrayList<String>(sequence);
						extendedSequence.add(listOfEvents.get(k));
						
						queue.offer(extendedSequence);
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 * check if a sequence covers a subseq or not
	 * 
	 * @param sequence
	 * @param subseq
	 * @return
	 */
	public boolean isCovered(List<String> sequence, List<String> subseq) {
		if (sequence.size() < subseq.size())
			return false;
		
		int i=0;
		int j=0;
		List<String> sequenceRightPart = new ArrayList<String>(sequence);
		for (; i<subseq.size(); i++){
			if (true == sequenceRightPart.contains(subseq.get(i))) {
				j = sequenceRightPart.indexOf(subseq.get(i)) + 1; // choose the right part after covered event
				sequenceRightPart = sequenceRightPart.subList(j, sequenceRightPart.size());
			}
			else
				return false;
		}
		
		// all events in the subseq can be found at the sequence in the same order 
		if (i == subseq.size())
			return true;

		return false;
	}
	
	/**
	 * @return the setOfTestSequences
	 */
	public Set<List<String>> getSetOfTestSequences() {
		return setOfTestSequences;
	}
	
	/**
	 * @return the setOfUncoveredTargetSeqs
	 */
	public Set<List<String>> getSetOfUncoveredTargetSeqs() {
		return setOfUncoveredTargetSeqs;
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
