package main;

import java.util.List;

import generator.SequenceGeneratorByAutomaton;
import generator.Subsequences;
import sequencingConstraints.Constraint;
import sequencingConstraints.SolverByAutomaton;
import sequencingConstraints.TypeInfo;
import sequencingConstraints.constraintParser.ConstraintParser;

/**
 * Here is an example to show how to generate t-way test sequences for SUT 
 * (System Under Test) with sequencing constraints.
 * Note that the constraint notation follows the last paper in my dissertation.
 * 
 * @author Feng Duan
 *
 */
public class TestSeqGen {
	public static void main(String[] args) {
		boolean isSolverDebugModeFlag = false;
//		boolean isGeneratorDebugModeFlag = true;
		boolean isGeneratorDebugModeFlag = false;
		
//		// SUT 1. Motivating example {Open, Read, Write, Close}
//		String[] events = {"Open", "Read", "Write", "Close"};
//		
//		String[] constraints = {
//			"Open +... Close", // Cons1: an open file must be closed in time
//			"(_ Open && Open .~. Close) -+ {Read, Write, Close}", // Cons2: Read/Write/Close file operations require the file must be open
//		};
//		
//		int smallestT = 1;
//		int largestT = 4;
//		
//		int smallestMaxRepetition = 1;
//		int largestMaxRepetition = 4;
//		
//		int smallestMaxLength = 1;
//		int largestMaxLength = 16; // events.length * largestT
//		// Motivating example end
		
		// SUT 2. case study start (The latest PHD constraints)
		/* the set of events  */
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
			"{RxAssocReq_unacceptable_configuration/TxAssocRsp_rejected, RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config} ...+ {RxConfigEventReportReq, REQAgentSuppliedUnsupportedConfig/TxConfigEventReportRsp_unsupported_config, REQAgentSuppliedSupportedConfig/TxConfigEventReportRsp_accepted_config, RxAssocRelReq/TxAssocRelRsp, REQAssocRel/TxAssocRelReq, REQAssocRel, RxAssocRelRsp}",
			"{RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config, RxConfigEventReportReq, REQAgentSuppliedUnsupportedConfig/TxConfigEventReportRsp_unsupported_config, REQAgentSuppliedSupportedConfig/TxConfigEventReportRsp_accepted_config, REQAssocRel/TxAssocRelReq, REQAssocRel} +... {RxAssocRelReq/TxAssocRelRsp, RxAssocRelRsp, RxAssocReq_unacceptable_configuration/TxAssocRsp_rejected}",
			
			"(_ {RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config, REQAgentSuppliedUnsupportedConfig/TxConfigEventReportRsp_unsupported_config} && {RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config, REQAgentSuppliedUnsupportedConfig/TxConfigEventReportRsp_unsupported_config} .~. {RxConfigEventReportReq, RxAssocRelReq/TxAssocRelRsp, REQAssocRel/TxAssocRelReq}) -+ RxConfigEventReportReq",
			"RxConfigEventReportReq -+ {REQAgentSuppliedUnsupportedConfig/TxConfigEventReportRsp_unsupported_config, REQAgentSuppliedSupportedConfig/TxConfigEventReportRsp_accepted_config}",
			
			// Abbr. form is "REQAssocRel/TxAssocRelReq ... RxAssocRelRsp", h ... j = (_ h && h .~. j) -+ j && h +- (_ j && h .~. j)
			"(_ REQAssocRel/TxAssocRelReq && REQAssocRel/TxAssocRelReq .~. RxAssocRelRsp) -+ RxAssocRelRsp && REQAssocRel/TxAssocRelReq +- (_ RxAssocRelRsp && REQAssocRel/TxAssocRelReq .~. RxAssocRelRsp)",
			
			"(_ {RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config} && {RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config} .~. {RxAssocRelReq/TxAssocRelRsp, REQAssocRel/TxAssocRelReq}) -+ REQAssocRel/TxAssocRelReq",
			"((_ {RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config} && {RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config} .~. {RxAssocRelReq/TxAssocRelRsp, REQAssocRel/TxAssocRelReq}) || (_ REQAssocRel/TxAssocRelReq && REQAssocRel/TxAssocRelReq .~. RxAssocRelRsp)) -+ RxAssocRelReq/TxAssocRelRsp",
			"(_ REQAssocRel/TxAssocRelReq && REQAssocRel/TxAssocRelReq .~. RxAssocRelRsp) -+ REQAssocRel",
			"((_ {RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config} && {RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config} .~. {RxAssocRelReq/TxAssocRelRsp, REQAssocRel/TxAssocRelReq}) || (_ REQAssocRel/TxAssocRelReq && REQAssocRel/TxAssocRelReq .~. RxAssocRelRsp)) ~ {RxAssocReq_unacceptable_configuration/TxAssocRsp_rejected, RxAssocReq_acceptable_and_known_configuration/TxAssocRsp_accepted, RxAssocReq_acceptable_and_unknown_configuration/TxAssocRsp_accepted_unknown_config}",
		};
		
		int smallestT = 1;
		int largestT = 3;
		
		int smallestMaxRepetition = 1;
		int largestMaxRepetition = 3; // largestT
		
		int smallestMaxLength = 1;
		int largestMaxLength = 30; // events.length * largestT
		// case study end
		
		String constraint = "";
		if (constraints.length >= 1)
		{
			constraint = constraints[0];
			for (int i=1; i<constraints.length; i++) {
				constraint += " && " + constraints[i];
			}
		}
		
		int[][][] numberOfTwaySeqs = new int[largestT+1][largestMaxRepetition+1][largestMaxLength+1];
		int[][][] numberOfCoveredSeqs = new int[largestT+1][largestMaxRepetition+1][largestMaxLength+1];
		
		float [][][] genTimeOfTestSeqs = new float[largestT+1][largestMaxRepetition+1][largestMaxLength+1];
		int[][][] numberOfTestSeqs = new int[largestT+1][largestMaxRepetition+1][largestMaxLength+1];
		
		int[][][] minLenOfTestSeqs = new int[largestT+1][largestMaxRepetition+1][largestMaxLength+1];
		float[][][] avgLenOfTestSeqs = new float[largestT+1][largestMaxRepetition+1][largestMaxLength+1];
		int[][][] maxLenOfTestSeqs = new int[largestT+1][largestMaxRepetition+1][largestMaxLength+1];
		
		int counter = 0;
		int t;
		int maxRepetition;
		int maxLength;
		for (t = smallestT; t<=largestT; t++) {
			int currentlargestMaxRepetition = largestMaxRepetition < t ? largestMaxRepetition : t;
			for (maxRepetition = smallestMaxRepetition; maxRepetition <= currentlargestMaxRepetition;  maxRepetition ++)
			{
				int maxLenFromMaxRepetition = maxRepetition * events.length;
				int currentlargestMaxLength = largestMaxLength < maxLenFromMaxRepetition ? largestMaxLength : maxLenFromMaxRepetition;
				int currentsmallestMaxLength = smallestMaxLength < currentlargestMaxLength ? smallestMaxLength : currentlargestMaxLength;
				currentsmallestMaxLength = currentsmallestMaxLength < t ? t : currentsmallestMaxLength; // max length should be no less than t
				for (maxLength = currentsmallestMaxLength; maxLength <= currentlargestMaxLength; maxLength++) {
					counter++;
					System.out.println("----------------------------");
					System.out.println(counter + "th Generation Begin! " + " Strength = " + t + 
							" , maxRepetition = " + maxRepetition + " , maxLength = " + maxLength);
					
					Subsequences subseqs = new Subsequences(events, t, maxRepetition);
					System.out.println(subseqs.getListOfEvents().size() + " Events: ");
					System.out.println("  " + subseqs.getListOfEvents());
					
					System.out.println("# of " + t + "-way permutations (with repetition <=" + maxRepetition + ") =  " + subseqs.getSetOfTwaySubseqs().size());
					System.out.println("  " + subseqs.getSetOfTwaySubseqs());
					
					System.out.println("Repetition Constraints: ");
					System.out.println("  " + "0 <= Event.# <= " + maxRepetition);
					
					System.out.println("Length Constraints: ");
					System.out.println("  " +  "1 <= Len <= " + maxLength);
					
					System.out.println("Sequencing Constraints: ");
					System.out.println("  " + constraint);
					
					System.out.println("");
					
					numberOfTwaySeqs[t][maxRepetition][maxLength] = subseqs.getSetOfTwaySubseqs().size();
										
					List<String> eventsList = subseqs.getListOfEvents();
					
					ConstraintParser parser = new ConstraintParser(
							constraint, eventsList);
					try {
						TypeInfo ti = parser.parse();
			
						Constraint sequencingCons = ti.getConstraint();
						
						/* ConstraintSolver */
						SolverByAutomaton solver = new SolverByAutomaton(isSolverDebugModeFlag, 
								eventsList, sequencingCons, maxRepetition, maxLength);
						
						/* SequenceGenerator */
						SequenceGeneratorByAutomaton generator = new SequenceGeneratorByAutomaton(eventsList, solver, subseqs.getSetOfTwaySubseqs());
						generator.setDebugMode(isGeneratorDebugModeFlag);
						// based on above informations, generate valid consecutive complete sequences to cover all target sequences 
						long genStartTime = System.currentTimeMillis();
						generator.generate();
						long genEndTime = System.currentTimeMillis();
						
						long genTime = genEndTime - genStartTime;
						int numOfTestSeqs = generator.getSetOfTestSequences().size();
						float sumOfLens = 0;
						int minLen = Integer.MAX_VALUE;
						int maxLen = Integer.MIN_VALUE;
						
						System.out.println("");
						System.out.println("Final Results:");
						System.out.print("# of generated sequences = " + generator.getSetOfTestSequences().size() + " , in lengths ( ");
						for (List<String> sequence : generator.getSetOfTestSequences()) {
							System.out.print(sequence.size() + " ");
							
							sumOfLens += sequence.size();
							
							if (sequence.size() < minLen)
								minLen = sequence.size();
							if (sequence.size() > maxLen)
								maxLen = sequence.size();
						}
						System.out.println(")");
						for (List<String> sequence : generator.getSetOfTestSequences()) {
							System.out.println(sequence);
						}
						
						System.out.println("===========================");
						System.out.println("");
						
						numberOfCoveredSeqs[t][maxRepetition][maxLength] = numberOfTwaySeqs[t][maxRepetition][maxLength] - generator.getSetOfUncoveredTargetSeqs().size();
						genTimeOfTestSeqs[t][maxRepetition][maxLength] = (float) (genTime)/1000;
						numberOfTestSeqs[t][maxRepetition][maxLength] = numOfTestSeqs;
						minLenOfTestSeqs[t][maxRepetition][maxLength] = minLen;
						avgLenOfTestSeqs[t][maxRepetition][maxLength] = sumOfLens/numOfTestSeqs;
						maxLenOfTestSeqs[t][maxRepetition][maxLength] = maxLen;
						
					} catch (Exception ex) {
						System.out.print(ex);
					}
					
				}
			}
		}
		
		System.out.println("t\t maxRepetition\t maxLength\t #OfT-waySeqs\t #OfCoveredSeqs\t genTime(sec)\t #OfTestSeqs\t minLen\t avgLen\t maxLen\t");
		for (t = smallestT; t<=largestT; t++) {
			int currentlargestMaxRepetition = largestMaxRepetition < t ? largestMaxRepetition : t;
			for (maxRepetition = smallestMaxRepetition; maxRepetition <= currentlargestMaxRepetition;  maxRepetition ++)
			{
				int maxLenFromMaxRepetition = maxRepetition * events.length;
				int currentlargestMaxLength = largestMaxLength < maxLenFromMaxRepetition ? largestMaxLength : maxLenFromMaxRepetition;
				int currentsmallestMaxLength = smallestMaxLength < currentlargestMaxLength ? smallestMaxLength : currentlargestMaxLength;
				currentsmallestMaxLength = currentsmallestMaxLength < t ? t : currentsmallestMaxLength; // max length should be no less than t
				for (maxLength = currentsmallestMaxLength; maxLength <= currentlargestMaxLength; maxLength++) {
					System.out.println(t + "\t" + maxRepetition + "\t" + maxLength + "\t"
							+ numberOfTwaySeqs[t][maxRepetition][maxLength] + "\t"
							+ numberOfCoveredSeqs[t][maxRepetition][maxLength] + "\t"
							+ genTimeOfTestSeqs[t][maxRepetition][maxLength] + "\t"
							+ numberOfTestSeqs[t][maxRepetition][maxLength] + "\t"
							+ minLenOfTestSeqs[t][maxRepetition][maxLength] + "\t"
							+ avgLenOfTestSeqs[t][maxRepetition][maxLength] + "\t"
							+ maxLenOfTestSeqs[t][maxRepetition][maxLength] + "\t");
				}
			}
		}
	}

}
