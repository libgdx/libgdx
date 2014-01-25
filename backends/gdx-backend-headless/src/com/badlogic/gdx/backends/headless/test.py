with open("Headless.java") as fin:
    data = fin.read()
    print data[-1] == "\n"
