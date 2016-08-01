# Introduktion

Klienten er til at arbejde med et bitmagasin hostet af SB og kan køres på både Windows og Linux miljøer, det eneste der kræves for at klienten kan køre er at maskinen har Java 8 installeret.

## Bitmagasinet
Bitmagasinet er et system til langtidsopbevaring af filer i flere uafhængige kopier for at sikre bit sikkerheden. 
Komponenterne i bitmagasinet som gemmer filer kaldes for en *pillar* (eller et *ben* på dansk). En fil gemmes derfor i et antal ligeværdige kopier på et antal *pillars*. 
En *pillar* kan indgå i et antal *collections* (samlinger på dansk). 

Operationerne som klienten tilbyder arbejder alle (på nær 'makechecksums') på en *collection*. Enkelte operationer arbejder ikke på alle pillars i en *collection*, og kræver derfor også angivelse af en specifik *pillar*. 

## Sumfilen
De forskellige operationer som klienten tilbyder arbejder med en sumfil. Sumfilen afspejler hvordan filerne vil se ud på den lokale maskine. Klienten tilbyder funktionalitet til at lave en sumfil enten ud fra et lokalt filtræ eller ud fra filer som allerede ligger i bitmagasinet. 

En sumfils indhold er en liste med filnavne og de enkelte filers MD5 checksum. 

# Operationer
De forskellige operationer som klienten tilbyder er: 

 * *upload* - Upload af filer ud fra en sumfil
 * *download* - Download af filer ud fra en sumfil
 * *delete* - Slette filer ud fra en sumfil
 * *makechecksums* - Lav en sumfil ud fra et lokalt filtræ
 * *list* - Lav en sumfil ud fra eksisterende filer i bitmagasinet

De tre operationer som arbejder på sumfiler, udviser alle idempotent adfærd - samme operation kan altså sikkert køres igen hvis der fx skulle opstå en fejl undervejs, eller operationen måtte blive afbrudt.

I løbet af kørslen af en operation vil der blive udskrevet status for de enkelte filer der arbejdes på. De mulige statuser er: 

 * *Started* - Operationen for filen er påbegyndt
 * *Skipped* - Filen springes over da den er filtreret fra (se de enkelt operationers beskrivelser) 
 * *Finished* - Operationen for filen er afsluttet med success
 * *Failed* - Operationen for filen fejlede

Når en kørsel afsluttes udskrives der desuden en status som opsummere de forskellige statuser.

## Filnavne og prefixer
De fleste operationer i klienten giver mulighed for brug af 'lokale' og 'bitmagasin' prefixes. Disse prefixes har til formål at begrænse hvilke filer der arbejdes på, samt at ændre filnavne mellem lokal og bitmagasin siden. 
Virkemåden af lokale og bitmagasin prefix illustreres bedst med de følgende eksempler:

### Ingen prefix
Lokalt prefix: *Ingen*, Bitmagasin prefix: *Ingen*

Lokal til bitmagasin:
```
mappe1/fil1 -> mappe1/fil1
mappe2/fil2 -> mappe2/fil2
```

Bitmagasin til lokal
```
mappe1/fil1 -> mappe1/fil1
mappe2/fil2 -> mappe2/fil2
```

### Kun lokalt prefix
Lokalt prefix: '*mappe1/*', Bitmagasin prefix: *Ingen*

Lokal til bitmagasin:
```
mappe1/fil1 -> fil1
mappe2/fil2 -> SKIPPED
```

Bitmagasin til lokal
```
mappe1/fil1 -> mappe1/mappe1/fil1
mappe2/fil2 -> mappe1/mappe2/fil2
```

### Kun bitmagasin prefix
Lokalt prefix: *Ingen*, Bitmagasin prefix: '*mappe1/*'

Lokal til bitmagasin:
```
mappe1/fil1 -> mappe1/mappe1/fil1
mappe2/fil2 -> mappe1/mappe2/fil2
```

Bitmagasin til lokal
```
mappe1/fil1 -> fil1
mappe2/fil2 -> SKIPPED
```

### Både lokalt og bitmagasin prefix 
Lokalt prefix: '*mappe1/*', Bitmagasin prefix: '*mappe3/*'

Lokal til bitmagasin:
```
mappe1/fil1 -> mappe3/fil1
mappe2/fil2 -> SKIPPED
```

Bitmagasin til lokal
```
mappe1/fil1 -> SKIPPED
mappe2/fil2 -> SKIPPED
mappe3/fil3 -> mappe1/fil3
```

## Operations beskrivelser

### Upload
Upload operationen anvender en sumfil til at afgøre hvilke filer der skal uploades til repositoriet. 
Hvis en fil allerede eksistere i repositoriet (samme navn og checksum), så vil klienten undlade at uploade den igen. 

#### Parametre

 * **-c** Bitmagasin samlingen som der skal uploades filer til
 * **-f** Sumfil med filer der skal arbejdes på         
 * **-l** Lokalt prefix, kun filer som fremgår i sumfilen med dette prefix vil blive uploaded. Prefixet fjernes
 * **-r** Bitmagasin prefix, tilføjes filnavnet i bitmagasin enden. 
 * **-n** Antallet af parallele operationer 
 * **-x** Antal af automatiske genforsøg

 
### Download
Download operationen anvender en sumfil til at afgøre hvilke filer der skal hentes fra repositoriet til klient maskinen. 
Filer der allerede eksistere på klient maskinen springes over. 

#### Parametre

 * **-c** Bitmagasin samlingen som der skal downloades filer fra
 * **-f** Sumfil med filer der skal arbejdes på
 * **-l** Lokalt prefix, kun filer som fremgår i sumfilen med dette prefix vil blive downloaded. Prefixet tilføjes filnavnet fra bitmagsinet
 * **-r** Bitmagsin prefix, fjernes fra filnavnet i bitmagasin enden. 
 * **-n** Antallet af parallele operationer
 * **-x** Antal af automatiske genforsøg


### Delete
Delete operationen anvender en sumfil til at afgøre hvilke filer der skal slettes fra repositoriet. 

#### Parametre

 * **-c** Bitmagsin samlingen som der skal slettes filer fra
 * **-f** Sumfil med filer der skal arbejdes på
 * **-p** Pillar som operationen udføres på. 
 * **-l** Lokalt prefix, kun filer som fremgår i sumfilen med dette prefix vil blive slettet. 
 * **-r** Bitmagasin prefix, tilføjes filnavnet i bitmagsin enden 
 * **-n** Antallet af parallele operationer
 * **-x** Antal af automatiske genforsøg


### Makechecksums
Makechecksums laver en sumfil ud fra et lokalt filtræ på maskinen som kører klienten. Hver fil som eksistere i filtræet får beregnet en MD5 checksum.

#### Parametre

 * **-f** Sumfil som der skal skrives til. Filen må ikke eksistere før genereringen
 * **-s** Sti som er udgangspunktet for filtræet der skal laves en sumfil til. Stien indgår i filnavnet og kan både være relativ og absolut. 


### List
List operationen henter filnavne og checksummer fra et ben i bitmagasinet og laver på baggrund af de informationer en sumfil til at lave andre operationer på baggrund af. 

#### Parametre

 * **-c** Bitmagasin samlingen der skal hentes filnavne og checksummer fra
 * **-f** Sumfil som der skal skrives til. Filen må ikke eksistere før operationen
 * **-p** Pillar som operationen udføres på
 * **-l** Lokalt prefix. Prefixet tilføjes filnavnet fra bitmagsinet
 * **-r** Bitmagsin prefix, kun filer med dette prefix i bitmagasin enden vil blive skrevet i sumfilen. Prefixet fjernes fra filnavnet. 


# Eksempler 
eksempler 
