package registerAllo;

import java.util.*;

public class hello {

    private static final int NUM_REGISTERS = 8;  // Assume we have 8 registers available
    private static final int MAX_SPILLED_REGISTERS = 4;  // Max number of spilled variables
    
    // Data structures for variables, live ranges, and register assignments
    private List<Variable> variables = new ArrayList<>();
    private List<Interval> intervals = new ArrayList<>();
    private Map<String, Integer> registerAssignment = new HashMap<>();
    private Set<String> spilledVariables = new HashSet<>();
    
    // This method simulates the live range analysis and assigns registers
    public void performRegisterAllocation(List<String> instructions) {
        // Step 1: Analyze the live ranges of variables
        analyzeLiveRanges(instructions);

        // Step 2: Allocate registers to variables using linear scan
        allocateRegisters();

        // Step 3: Handle any spilled variables
        handleSpilling();

        // Output the results
        System.out.println("Register Assignments: " + registerAssignment);
        System.out.println("Spilled Variables: " + spilledVariables);
    }

    // Analyze the live ranges of variables in the program
    private void analyzeLiveRanges(List<String> instructions) {
        int currentIndex = 0;
        for (String instruction : instructions) {
            // Identify variables used and defined in each instruction
            String[] parts = instruction.split(" ");
            for (String part : parts) {
                if (part.matches("[a-zA-Z]+")) {  // Assume variables are alphabetic
                    Interval interval = new Interval(part, currentIndex, currentIndex);
                    intervals.add(interval);
                    variables.add(new Variable(part, interval));
                }
            }
            currentIndex++;
        }
    }

    // Allocate registers using linear scan algorithm
    private void allocateRegisters() {
        Collections.sort(intervals, Comparator.comparingInt(i -> i.start));  // Sort by start of intervals
        
        // Active list of variables in registers
        List<Variable> activeVars = new ArrayList<>();
        
        for (Interval interval : intervals) {
            // Remove variables from active list that are no longer live
            activeVars.removeIf(var -> var.interval.end < interval.start);
            
            // Check if the variable can be coalesced
            if (activeVars.size() < NUM_REGISTERS) {
                registerAssignment.put(interval.variableName, activeVars.size());
                activeVars.add(new Variable(interval.variableName, interval));
            } else {
                // Spill the least recently used variable
                Variable spilledVar = activeVars.get(0);
                spilledVariables.add(spilledVar.name);
                activeVars.remove(0);
                registerAssignment.put(interval.variableName, -1);  // -1 for spilled variables
                activeVars.add(new Variable(interval.variableName, interval));
            }
        }
    }

    // Handle spilling for variables that couldn't be assigned to registers
    private void handleSpilling() {
        if (spilledVariables.size() > MAX_SPILLED_REGISTERS) {
            // A more sophisticated spilling mechanism could be applied here
            System.out.println("Too many variables spilled! Consider optimizing live range splitting.");
        }
    }
    
    // Class representing a variable with its live range interval
    class Variable {
        String name;
        Interval interval;
        
        Variable(String name, Interval interval) {
            this.name = name;
            this.interval = interval;
        }
    }
    
    // Class representing an interval for a variable's live range
    class Interval {
        String variableName;
        int start, end;
        
        Interval(String variableName, int start, int end) {
            this.variableName = variableName;
            this.start = start;
            this.end = end;
        }
    }

    public static void main(String[] args) {
        hello allocator = new hello();
        
        // Sample instructions (simple example, real instructions would be more complex)
        List<String> instructions = Arrays.asList(
                "a = b + c",
                "d = a * b",
                "e = a + d",
                "f = e - b",
                "g = f + a"
        );
        
        allocator.performRegisterAllocation(instructions);
    }
}
