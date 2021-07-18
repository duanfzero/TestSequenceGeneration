package generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * enumerate subsequences as the t-way target sequence candidates 
 * 
 * @author Feng Duan
 *
 */
public class Subsequences {
	
	private List<String> listOfEvents;
	private Set<List<String>> setOfTwaySubseqs;

	public Subsequences(String[] events, int t, int maxRepetition) {
		listOfEvents = new ArrayList<String>();
		for (String event : events)
			listOfEvents.add(event);
		
		setOfTwaySubseqs = new LinkedHashSet<List<String>>();
		
		// Enumerate and store all t-way permutations (with repetition) of events, e.g.,
		// given events {A, B, C, D, E} (n=5), strength (t=3), repetition <= maxRepetition (maxRepetition = 2)
		// we can enumerate 5^3-5 = 125-5 = 120 3-way subsequences:
		//  <A, A, B>
		//  ...
		//  <A, E, E>
		//  ...
		// [A, A, A][B, B, B][C, C, C][D, D, D][E, E, E] is invalid on repetition constraint #<=2
		// Note that, some enumerated subsequences may be invalid due to constraints, 
		// thus we will do validity check to only store valid subsequences.
		ArrayList<int[]> eventPowers = getEventPowers(listOfEvents.size(), t);
//		System.out.println(eventPowers.size());
//		for (int[] eventCombo : eventPowers) {
//			System.out.print("[");
//			for (int i : eventCombo)
//				System.out.print(i + ",");
//			System.out.println("]");
//		}
		
		for (int[] eventPower : eventPowers) {
			List<String> subseq = new ArrayList<String>();
			for (int eventIndex : eventPower)
	            subseq.add(listOfEvents.get(eventIndex));
	        
			if (isValidOnRepetition(subseq, maxRepetition))
				setOfTwaySubseqs.add(subseq);
//			else 
//				System.out.println(subseq + " is invalid on repetition constraint #<=" + maxRepetition);
		}
	}
	
	// Consider n events have indices as 0 to n-1, 
	// then n^t can be enumerated as follows.
	public ArrayList<int[]> getEventPowers(int n, int t) {
		ArrayList<int[]> rval = new ArrayList<int[]>();
		
		int[] index = new int[t];
		for (int i=0; i<t; i++)
			index[i] = 0;
		
		boolean exhaustedFlag = false;
		while (!exhaustedFlag){
			// create a deep copy of index
			addCombo(rval, index);
						
			int pos = t-1; // last event slot
			
			boolean carryFlag = false;
			do {
				index[pos]++;
				if (index[pos] > n-1) {// carry
					carryFlag = true;
					
					index[pos] = 0;
					pos--;
					if (pos < 0) {// carry from top event slot means exhausted
						exhaustedFlag = true;
						carryFlag = false;
					}
				}
				else
					carryFlag = false;
			} while (carryFlag);
		}
		
		return rval;
	}
	
	private void addCombo(ArrayList<int[]> combos, int[] index) {
		int[] combo = new int[index.length];
		for (int i = 0; i < index.length; i++) {
			combo[i] = index[i];
		}
		combos.add(combo);
	}
	
	public List<String> getListOfEvents(){
		return listOfEvents;
	}
	
	public Set<List<String>> getSetOfTwaySubseqs(){
		return setOfTwaySubseqs;
	}
	
	public boolean isValidOnRepetition(List<String> ciSeq, int maxRepetition){
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
			if (count > maxRepetition)
				return false;
		}
		
		return true;
	}

	public static void main(String[] args){
		String[] events = {"A", "B", "C", "D", "E"};
		int t = 3; // strength of subsequences
		int maxRepetition = 2; // repetition constraint
		
		Subsequences subseqs = new Subsequences(events, t, maxRepetition);
		System.out.println(subseqs.getListOfEvents());
		
		System.out.println("# of " + t + "-way permutations (with repetition <=" + maxRepetition + ") =  " + subseqs.getSetOfTwaySubseqs().size());
		System.out.println(subseqs.getSetOfTwaySubseqs());
	}
}
