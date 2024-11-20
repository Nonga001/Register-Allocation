package registerAllo;

import java.util.*;

public class hello {

    private static final int NUM_REGISTERS = 8;  // Assume we have 8 registers available
    private static final int MAX_SPILLED_REGISTERS = 4;  // Max number of spilled variables
    
    private List<Variable> variables = new ArrayList<>();
    private List<Interval> intervals = new ArrayList<>();
    private Map<String, Integer> registerAssignment = new HashMap<>();
    private Set<String> spilledVariables = new HashSet<>();
    
    // Perform register allocation and measure performance
    public void performRegisterAllocation(List<String> instructions) {
        long startTime = System.nanoTime();  // Start time for execution time measurement

        // Step 1: Analyze the live ranges of variables
        analyzeLiveRanges(instructions);

        // Step 2: Allocate registers to variables using linear scan
        allocateRegisters();

        // Step 3: Handle any spilled variables
        handleSpilling();

        long endTime = System.nanoTime();  // End time for execution time measurement
        long duration = endTime - startTime;  // Duration in nanoseconds

        // Print the results
        System.out.println("Register Assignments: " + registerAssignment);
        System.out.println("Spilled Variables: " + spilledVariables);
        System.out.println("Execution Time: " + duration + " ns");
        System.out.println("Number of Spills: " + spilledVariables.size());
        System.out.println("Memory Usage: " + getMemoryUsage() + " bytes");
    }

    // Analyze the live ranges of variables in the program
    private void analyzeLiveRanges(List<String> instructions) {
        int currentIndex = 0;
        for (String instruction : instructions) {
            String[] parts = instruction.split(" ");
            for (String part : parts) {
                if (part.matches("[a-zA-Z]+")) {
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
        Collections.sort(intervals, Comparator.comparingInt(i -> i.start));
        
        List<Variable> activeVars = new ArrayList<>();
        
        for (Interval interval : intervals) {
            activeVars.removeIf(var -> var.interval.end < interval.start);
            
            if (activeVars.size() < NUM_REGISTERS) {
                registerAssignment.put(interval.variableName, activeVars.size());
                activeVars.add(new Variable(interval.variableName, interval));
            } else {
                Variable spilledVar = activeVars.get(0);
                spilledVariables.add(spilledVar.name);
                activeVars.remove(0);
                registerAssignment.put(interval.variableName, -1);
                activeVars.add(new Variable(interval.variableName, interval));
            }
        }
    }

    private void handleSpilling() {
        if (spilledVariables.size() > MAX_SPILLED_REGISTERS) {
            System.out.println("Too many variables spilled! Consider optimizing live range splitting.");
        }
    }

    // Method to measure memory usage
    private long getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
        return usedMemoryBefore;
    }
    
    // Variable class represents a variable and its live range
    class Variable {
        String name;
        Interval interval;
        
        Variable(String name, Interval interval) {
            this.name = name;
            this.interval = interval;
        }
    }
    
    // Interval class represents the live range of a variable
    class Interval {
        String variableName;
        int start, end;
        
        Interval(String variableName, int start, int end) {
            this.variableName = variableName;
            this.start = start;
            this.end = end;
        }
    }

    // Main method to accept input and evaluate the allocator
    public static void main(String[] args) {
        hello allocator = new hello();
        
        // Example input program (sample instructions)
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your program instructions (type 'end' to finish):");
        
        List<String> instructions = new ArrayList<>();
        String line;
        
        while (!(line = scanner.nextLine()).equals("end")) {
            instructions.add(line);
        }
        
        // Perform register allocation
        allocator.performRegisterAllocation(instructions);
        
        // You can extend this with other test cases or compare to a naive allocator here
    }
}
