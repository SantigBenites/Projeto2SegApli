import sys, socket, threading, os
from utils import *
from MITMConnection import *

def main(argv):
    
    argv = stringToArgs("".join(argv))

    # Terminal line inputs
    mitmPort        = argv[argv.index("-p")+1] if "-p" in argv else 4000
    serverIpAddress = argv[argv.index("-s")+1] if "-i" in argv else "127.0.0.1"
    serverPort      = argv[argv.index("-q")+1] if "-p" in argv else 3000
    try:

        clientSocket = createSocket(port=mitmPort)
        print(f"MITM started in port {mitmPort}")

        clientSocket.listen()
        conn, addr = clientSocket.accept()

        bankSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        bankSocket.connect((serverIpAddress, serverPort))

        while True:
            
            handShake(conn,bankSocket)
            
            clientMessage = clientReceive(conn)
            bankSend(bankSocket,clientMessage)

            bankMessage = bankReceive(bankSocket)
            clientSend(conn,bankMessage)

    except KeyboardInterrupt:
        print("Ended Properly\n")

        dir = f"{os.getcwd()}/Grupo1/Executables"
        jarFiles = ["Bank.jar","MBeC.jar","Store.jar"]
        for f in os.listdir(dir):
            if f not in jarFiles:
                os.remove(f"{os.getcwd()}/Grupo1/Executables/{f}")
        if os.path.isfile(f"{os.getcwd()}/Grupo1/Executables/bank.auth"):
            os.remove(f"{os.getcwd()}/Grupo1/Executables/bank.auth")





if __name__ == "__main__":
    main(sys.argv[1:])
