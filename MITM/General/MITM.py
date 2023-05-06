import sys, socket, threading, os, queue
# directory reach
current = os.path.dirname(os.path.realpath(__file__))
# setting path
parent = os.path.dirname(current)
sys.path.append(parent)
from utils import *
from MITMConnection import *
from MITMThreads import *

def main(argv):
    
    argv = stringToArgs("".join(argv))

    # Terminal line inputs
    mitmPort        = argv[argv.index("-p")+1] if "-p" in argv else 5000
    serverIpAddress = argv[argv.index("-s")+1] if "-i" in argv else "127.0.0.1"
    serverPort      = argv[argv.index("-q")+1] if "-p" in argv else 6000
    try:

        threadsStarted = []  
        clientSocket = createSocket(port=mitmPort)

        while True:
            
            print(f"MITM started in port {mitmPort}")

            conn, addr = clientSocket.accept()
            conn.setblocking(0)
            print("Client connected")

            bankSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            bankSocket.connect((serverIpAddress, serverPort))

            forwardQueue = queue.Queue()
            backQueue = queue.Queue()

            # BankThread
            bankThread = threading.Thread(target=BankThread, args=(forwardQueue,backQueue,bankSocket))
            bankThread.start()
            threadsStarted.append(bankThread)

            # ClientThread
            clientThread = threading.Thread(target=ClientThread, args=(forwardQueue,backQueue,conn))
            clientThread.start()
            threadsStarted.append(clientThread)



    except KeyboardInterrupt:
        print("Ended Properly\n")

        bankSocket.close()
        clientSocket.close()
        
        for thread in threadsStarted:
            thread.join()

        deleteFiles()



def deleteFiles():
    dir = f"{os.getcwd()}/Grupo1/Executables"
    jarFiles = ["Bank.jar","MBeC.jar","Store.jar"]
    for f in os.listdir(dir):
        if f not in jarFiles:
            os.remove(f"{os.getcwd()}/Grupo1/Executables/{f}")
    if os.path.isfile(f"{os.getcwd()}/Grupo1/Executables/bank.auth"):
        os.remove(f"{os.getcwd()}/Grupo1/Executables/bank.auth")


if __name__ == "__main__":
    main(sys.argv[1:])
