SRC_DIR = src
BUILD_DIR = build
MAIN_FILE = main.testMP3Player
COMPILER_FLAGS = -g
JLAYER = lib/jlayer-1.0.3.jar

# Find all .java files in src
SOURCES := $(shell find $(SRC_DIR) -name "*.java")

all:
	# Create build directory if it doesn't exist
	mkdir -p $(BUILD_DIR)
	# Compile all Java files and specify output directory
	javac -cp $(JLAYER) $(COMPILER_FLAGS) -d $(BUILD_DIR) $(SOURCES)

run:
	# Run the main class with classpath set to the build directory
	java -cp $(BUILD_DIR):$(JLAYER) $(MAIN_FILE)

debug:
	# Start Java program in debug mode, waiting for debugger to attach on port 5005
	java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -cp $(BUILD_DIR) $(MAIN_FILE)

debug_attach:
	# Attach jdb to the running process on port 5005
	jdb -attach 5005

clean:
	# Remove all compiled files in the build directory
	rm -rf $(BUILD_DIR)/*
