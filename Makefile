SRC_DIR = src
BUILD_DIR = build

MAIN_FILE = ClientUDP


all:
	javac $(SRC_DIR)/$(MAIN_FILE).java -d $(BUILD_DIR)

run:
	java -cp $(BUILD_DIR) $(MAIN_FILE) $(IP) $(PORT)

clean:
	rm -r build/*