# Projeto2SegApli

2022/2023

Segurança Aplicada

TP21-G04:
- Santiago Benites fc54392
- Inês Morais fc54462
- Miguel Carvalho fc54399


# Running Attacks

## Replay Attack Group 1

We are able to see the value in account 5000 is 369 and not 236

MITM/Grupo1Replay

Terminal 1
``` python
python MITMG1Replay.py
```

Grupo1/Executables

Terminal 2
```
java -jar Bank.jar
```

Terminal 3
``` java
java -jar MBeC.jar -a 5000 -n 123.00 -u 5000.user -p 4000
java -jar MBeC.jar -a 5000 -d 123.00 -u 5000.user -p 4000
java -jar MBeC.jar -a 5000 -u 5000.user -g
```

## Crash Store Group 1

We are able to see the Store shutdown

Grupo1/Executables

Terminal 1
```
java -jar Bank.jar
```

Terminal 2
```
java -jar Store.jar
```

Terminal 3
``` java
java -jar MBeC.jar -a 5000 -n 123.00 -u 5000.user -p 4000
java -jar MBeC.jar -u 5000.user -a 5000 -c 100.00
java -jar MBeC.jar -u 5000.user -a 5000 -c 100.00
```

## Timing Attack

We should be able to see the time each connection took, which would allow some inference about the type of operation

MITM/General

Terminal 1
```
python MITM.py
```

Grupo1/Executables

Terminal 2
```
java -jar Bank.jar
```

Terminal 3
``` java
java -jar MBeC.jar -a 5000 -n 123.00 -u 5000.user -p 4000
java -jar MBeC.jar -a 5000 -d 123.00 -u 5000.user -p 4000
java -jar MBeC.jar -u 5000.user -a 5000 -c 100.00 -p 4000
```


## Integrety Attack Group 8

Terminal 1
```
java -jar Grupo8/src/bank/bank.jar 
```

Terminal 2
``` 
java -jar Grupo8/src/Client/mbec.jar -a 50 -n 1000.00
``` 

Terminal 3
``` 
java -jar MITM/grupo8userfile/grupo8userfile.jar
``` 

Wait until program stops

Terminal 2
``` 
java -jar Grupo8/src/Client/mbec.jar -a 50 -g
``` 


## Confidentiality Attack Group 8

Terminal 1
```
python MITM/General/MITM.py -p 5000 -q 4000
```

Terminal 2
```
java -jar Grupo8/src/store/store.jar
```

Terminal 3
```
java -jar Grupo8/src/bank/bank.jar 
```

Terminal 4
```
java -jar Grupo8/src/Client/mbec.jar -a 500 -n 1000.00 

java -jar Grupo8/src/Client/mbec.jar -a 500 -c 100.00 

java -jar Grupo8/src/Client/mbec.jar -v 500_1.card -m 70.00

```