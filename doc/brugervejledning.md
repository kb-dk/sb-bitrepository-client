# Introduktion

Klienten er til at arbejde med et bitmagasin hostet af SB og kan køres på både Windows og Linux miljøer. Det eneste der kræves for klienten kan køre er at maskinen har Java 8 installeret.

## Bitmagasinet
[Bitmagasinet](http://bitrepository.org) er et system til langtidsopbevaring af filer i flere uafhængige kopier for at sikre bit sikkerheden. 
Komponenterne i bitmagasinet som gemmer filer kaldes for en *pillar* (eller et *ben* på dansk). En fil gemmes derfor i et antal ligeværdige kopier på et antal *pillars*. 
En *pillar* kan indgå i et antal *collections* (samlinger på dansk). 

Operationerne som klienten tilbyder, arbejder alle (på nær 'makechecksums') på en *collection*. Enkelte operationer arbejder ikke på alle pillars i en *collection*, og kræver derfor også angivelse af en specifik *pillar*. 

## Sumfilen
De forskellige operationer som klienten tilbyder, arbejder med en sumfil. Sumfilen afspejler hvordan filerne vil se ud på den lokale maskine. Klienten tilbyder funktionalitet til at lave en sumfil enten ud fra et lokalt filtræ eller ud fra filer som allerede ligger i bitmagasinet. 

En sumfils indhold er en liste med filnavne og de enkelte filers MD5 checksum. 

# Operationer
De forskellige operationer som klienten tilbyder er: 

 * *upload* - Upload af filer ud fra en sumfil
 * *download* - Download af filer ud fra en sumfil
 * *delete* - Slette filer ud fra en sumfil
 * *makechecksums* - Lav en sumfil ud fra et lokalt filtræ
 * *list* - Lav en sumfil ud fra eksisterende filer i bitmagasinet

De tre operationer som arbejder på sumfiler, udviser alle idempotent adfærd - samme operation kan altså køres sikkert igen hvis der fx skulle opstå en fejl undervejs, eller operationen måtte blive afbrudt.

I løbet af kørslen af en operation vil der blive udskrevet status for de enkelte filer der arbejdes på. De mulige statuser er: 

 * *Started* - Operationen for filen er påbegyndt
 * *Skipped* - Filen springes over da den er filtreret fra (se de enkelt operationers beskrivelser) 
 * *Finished* - Operationen for filen er afsluttet med success
 * *Failed* - Operationen for filen fejlede

Når en kørsel afsluttes udskrives der desuden en opsummering af de forskellige statuser.

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

Bitmagasin til lokal:
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

Bitmagasin til lokal:
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

Bitmagasin til lokal:
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

Bitmagasin til lokal:
```
mappe1/fil1 -> SKIPPED
mappe2/fil2 -> SKIPPED
mappe3/fil3 -> mappe1/fil3
```

# Brug af klienten
Klienten er en kommandolinjeklient til brug på Linux (sbclient.sh) og Windows (sbclient.cmd).

Klienten styres af de argumenter og parametre som gives til klienten på kommandolinjen. 
Angives argumentet '-h', eller skulle der mangle påkrævede argumenter eller parametre udskriver klienten en hjælpe tekst. 

Operations typen angives som parameter til argumentet '-a'. Beskrivelse af yderligere argumenter til de enkelte operationer findes i de følgende afsnit.

## Operations beskrivelser

### Upload
Upload operationen anvender en sumfil til at afgøre hvilke filer der skal uploades til repositoriet. 
Hvis en fil allerede eksistere i repositoriet (samme navn og checksum), så vil klienten undlade at uploade den igen. 

#### Parametre

 * **-c** Bitmagasin samlingen som der skal uploades filer til
 * **-f** Sumfil med filer der skal arbejdes på         
 * **-l** Lokalt prefix, kun filer som fremgår i sumfilen med dette prefix vil blive uploaded. Prefixet fjernes
 * **-r** Bitmagasin prefix, tilføjes filnavnet i bitmagasinet
 * **-n** Antallet af parallelle operationer 
 * **-x** Antal af automatiske genforsøg

 
### Download
Download operationen anvender en sumfil til at afgøre hvilke filer der skal hentes fra repositoriet til klient maskinen. 
Filer der allerede eksistere på klient maskinen springes over. 

#### Parametre

 * **-c** Bitmagasin samlingen som der skal downloades filer fra
 * **-f** Sumfil med filer der skal arbejdes på
 * **-l** Lokalt prefix, kun filer som fremgår i sumfilen med dette prefix vil blive downloaded. Prefixet tilføjes filnavnet fra bitmagsinet
 * **-r** Bitmagsin prefix, fjernes fra filnavnet i bitmagasinet
 * **-n** Antallet af parallelle operationer
 * **-x** Antal af automatiske genforsøg


### Delete
Delete operationen anvender en sumfil til at afgøre hvilke filer der skal slettes fra repositoriet. 

#### Parametre

 * **-c** Bitmagsin samlingen som der skal slettes filer fra
 * **-f** Sumfil med filer der skal arbejdes på
 * **-p** Pillar som operationen udføres på
 * **-l** Lokalt prefix, kun filer som fremgår i sumfilen med dette prefix vil blive slettet
 * **-r** Bitmagasin prefix, tilføjes filnavnet i bitmagsinet 
 * **-n** Antallet af parallelle operationer
 * **-x** Antal af automatiske genforsøg


### Makechecksums
Makechecksums laver en sumfil ud fra et lokalt filtræ på maskinen som kører klienten. Hver fil som eksisterer i filtræet får beregnet en MD5 checksum.

#### Parametre

 * **-f** Sumfil som der skal skrives til. Filen må ikke eksistere før genereringen
 * **-s** Mappe hvis indhold der skal laves en sumfil for. Stien indgår i filnavnet og kan både være relativ og absolut. 


### List
List operationen henter filnavne og checksummer fra et ben i bitmagasinet og laver på baggrund af de informationer en sumfil. Denne sumfil kan anvendes til andre operationer. 

#### Parametre

 * **-c** Bitmagasin samlingen der skal hentes filnavne og checksummer fra
 * **-f** Sumfil som der skal skrives til. Filen må ikke eksistere før operationen
 * **-p** Pillar som operationen udføres på
 * **-l** Lokalt prefix. Prefixet tilføjes filnavnet fra bitmagsinet
 * **-r** Bitmagsin prefix, kun filer med dette prefix i bitmagasin enden vil blive skrevet i sumfilen. Prefixet fjernes fra filnavnet


# Eksempler 
Følgende eksempler skulle gerne illustrere de grundliggende funktionaliteter af klienten.

Der tages udgangspunkt i følgende filstruktur:
```
sb-client-ex
│ 
├── CD1
│     ├─ cd1-track1  (Checksum: 297abb31e43c7b8ae947fa2a59fe9d40)
│     ├─ cd1-track2  (Checksum: 2a005b7d68bbb88374a4bfb6e53b5537)
│     ├─ cd1-track3  (Checksum: b4dfc4e7e4061a10f1cee2f030957733)
│ 
├── CD2
│     ├─ cd2-track1  (Checksum: 297abb31e43c7b8ae947fa2a59fe9d40)
│     ├─ cd2-track2  (Checksum: 2a005b7d68bbb88374a4bfb6e53b5537)
│     ├─ cd2-track3  (Checksum: b4dfc4e7e4061a10f1cee2f030957733)
```
For hvert eksempel er kommandoerne der køres og de dertilhørende outputs angivet for både Linux og Windows udgaven.
For at anvende eksemplerne i din egen installation, skal de angavne *collections* og *pillars* rettes til så de stemmer overens med den lokale installation.

## Generering af første sumfil
For at lave en sumfil (sumfil1) ud fra det lokale fil træ (mappen 'sb-client-ex'), anvendes '*makechecksums*' operationen.

**Linux**:
```
sb-bitrepository-client/bin/sbclient.sh -a makechecksums -s sb-client-ex -f sumfil1
```

Dette skulle resultere i en ny fil 'sumfil1' med følgende indhold:
```
b4dfc4e7e4061a10f1cee2f030957733  sb-client-ex/CD1/cd1-track3
2a005b7d68bbb88374a4bfb6e53b5537  sb-client-ex/CD1/cd1-track2
297abb31e43c7b8ae947fa2a59fe9d40  sb-client-ex/CD1/cd1-track1
b4dfc4e7e4061a10f1cee2f030957733  sb-client-ex/CD2/cd2-track3
2a005b7d68bbb88374a4bfb6e53b5537  sb-client-ex/CD2/cd2-track2
297abb31e43c7b8ae947fa2a59fe9d40  sb-client-ex/CD2/cd2-track1
```

**Windows**
```
sb-bitrepository-client\bin\sbclient.cmd -a makechecksums -s sb-client-ex -f sumfil1
```

Dette skulle resultere i en ny fil 'sumfil1' med følgende indhold:
```
b4dfc4e7e4061a10f1cee2f030957733  sb-client-ex\CD1\cd1-track3
2a005b7d68bbb88374a4bfb6e53b5537  sb-client-ex\CD1\cd1-track2
297abb31e43c7b8ae947fa2a59fe9d40  sb-client-ex\CD1\cd1-track1
b4dfc4e7e4061a10f1cee2f030957733  sb-client-ex\CD2\cd2-track3
2a005b7d68bbb88374a4bfb6e53b5537  sb-client-ex\CD2\cd2-track2
297abb31e43c7b8ae947fa2a59fe9d40  sb-client-ex\CD2\cd2-track1
```

## Upload af filer
For at uploade filer til bitmagasinet skal upload operationen anvendes. 

Filerne som vi vil uploade ligger i mappen 'sb-client-ex' og den ønsker vi i bitmagasin enden skal hedde 'album1', og derfor skal vi bruge både lokale og bitmagasin prefixer. 
For at uploade filerne til samlingen 'CD-collection' køres følgende (outputtet findes efter kommandoen):

**Linux**
```
sb-bitrepository-client/bin/sbclient.sh -a upload -f sumfil1 -c CD-collection -l sb-client-ex/ -r album1/
[STARTING]: upload of sb-client-ex/CD1/cd1-track3
[FINISHED]: upload of sb-client-ex/CD1/cd1-track3
[STARTING]: upload of sb-client-ex/CD1/cd1-track2
[FINISHED]: upload of sb-client-ex/CD1/cd1-track2
[STARTING]: upload of sb-client-ex/CD1/cd1-track1
[FINISHED]: upload of sb-client-ex/CD1/cd1-track1
[STARTING]: upload of sb-client-ex/CD2/cd2-track3
[FINISHED]: upload of sb-client-ex/CD2/cd2-track3
[STARTING]: upload of sb-client-ex/CD2/cd2-track2
[FINISHED]: upload of sb-client-ex/CD2/cd2-track2
[STARTING]: upload of sb-client-ex/CD2/cd2-track1
[FINISHED]: upload of sb-client-ex/CD2/cd2-track1
Started: 6, Finished: 6, Failed: 0, Skipped: 0
```

**Windows**
```
sb-bitrepository-client\bin\sbclient.cmd -a upload -f sumfil1 -c CD-collection -l sb-client-ex\ -r album1\
[STARTING]: upload of sb-client-ex\CD1\cd1-track3
[FINISHED]: upload of sb-client-ex\CD1\cd1-track3
[STARTING]: upload of sb-client-ex\CD1\cd1-track2
[FINISHED]: upload of sb-client-ex\CD1\cd1-track2
[STARTING]: upload of sb-client-ex\CD1\cd1-track1
[FINISHED]: upload of sb-client-ex\CD1\cd1-track1
[STARTING]: upload of sb-client-ex\CD2\cd2-track3
[FINISHED]: upload of sb-client-ex\CD2\cd2-track3
[STARTING]: upload of sb-client-ex\CD2\cd2-track2
[FINISHED]: upload of sb-client-ex\CD2\cd2-track2
[STARTING]: upload of sb-client-ex\CD2\cd2-track1
[FINISHED]: upload of sb-client-ex\CD2\cd2-track1
Started: 6, Finished: 6, Failed: 0, Skipped: 0
```

## List filer i bitmagasinet
For at tjekke at filerne er kommet godt over i bitmagasinet og at de ligger i 'album1' fremfor 'sb-client-ex' kan list operationen anvendes:

**Linux**
```
sb-bitrepository-client/bin/sbclient.sh -a list -f sumfil2 -c CD-collection -p pillar1
```

Kørslen af operationen resultere i en ny sumfil, 'sumfil2', som indeholder: 
```
b4dfc4e7e4061a10f1cee2f030957733  album1/CD1/cd1-track3
2a005b7d68bbb88374a4bfb6e53b5537  album1/CD1/cd1-track2
297abb31e43c7b8ae947fa2a59fe9d40  album1/CD1/cd1-track1
b4dfc4e7e4061a10f1cee2f030957733  album1/CD2/cd2-track3
2a005b7d68bbb88374a4bfb6e53b5537  album1/CD2/cd2-track2
297abb31e43c7b8ae947fa2a59fe9d40  album1/CD2/cd2-track1
```

**Windows**
```
sb-bitrepository-client\bin\sbclient.cmd -a list -f sumfil2 -c CD-collection -p pillar1
```

Kørslen af operationen resultere i en ny sumfil, 'sumfil2', som indeholder: 
```
b4dfc4e7e4061a10f1cee2f030957733  album1\CD1\cd1-track3
2a005b7d68bbb88374a4bfb6e53b5537  album1\CD1\cd1-track2
297abb31e43c7b8ae947fa2a59fe9d40  album1\CD1\cd1-track1
b4dfc4e7e4061a10f1cee2f030957733  album1\CD2\cd2-track3
2a005b7d68bbb88374a4bfb6e53b5537  album1\CD2\cd2-track2
297abb31e43c7b8ae947fa2a59fe9d40  album1\CD2\cd2-track1
```


## Download af filer
Hvis sumfilen 'sumfil2' blot blev brugt til at hente filerne ned til klient maskinen igen, så ville alle filerne blive hentet hjem i ny mappe 'album1', da sumfilen altid afspejler hvordan filerne ser ud lokalt.
Ønskes filerne placeret et andet sted lokalt, så skal der anvendes lokal og bitmagasin prefixer fx:

**Linux**
```
sb-bitrepository-client/bin/sbclient.sh -a list -f sumfil3 -c CD-collection -p pillar1 -r album1/ -l lokal-album/
```

Hvilket vil resultere i en ny sumfil, 'sumfil3' med følgende indhold:
```
b4dfc4e7e4061a10f1cee2f030957733  lokal-album/CD1/cd1-track3
2a005b7d68bbb88374a4bfb6e53b5537  lokal-album/CD1/cd1-track2
297abb31e43c7b8ae947fa2a59fe9d40  lokal-album/CD1/cd1-track1
b4dfc4e7e4061a10f1cee2f030957733  lokal-album/CD2/cd2-track3
2a005b7d68bbb88374a4bfb6e53b5537  lokal-album/CD2/cd2-track2
297abb31e43c7b8ae947fa2a59fe9d40  lokal-album/CD2/cd2-track1
```

**Windows**
```
sb-bitrepository-client\bin\sbclient.cmd -a list -f sumfil3 -c CD-collection -p pillar1 -r album1\ -l lokal-album\
```

Hvilket vil resultere i en ny sumfil, 'sumfil3' med følgende indhold:
```
b4dfc4e7e4061a10f1cee2f030957733  lokal-album\CD1\cd1-track3
2a005b7d68bbb88374a4bfb6e53b5537  lokal-album\CD1\cd1-track2
297abb31e43c7b8ae947fa2a59fe9d40  lokal-album\CD1\cd1-track1
b4dfc4e7e4061a10f1cee2f030957733  lokal-album\CD2\cd2-track3
2a005b7d68bbb88374a4bfb6e53b5537  lokal-album\CD2\cd2-track2
297abb31e43c7b8ae947fa2a59fe9d40  lokal-album\CD2\cd2-track1
```


Ønsker vi nu kun at hente indholdet af 'CD2' kan download operation anvendes med: 

**Linux**
```
sb-bitrepository-client/bin/sbclient.sh -a download -f sumfil3 -c SB-devel-test1 -r album1/CD2/ -l lokal-album/CD2/
[SKIPPING]: download of lokal-album/CD1/cd1-track3
[SKIPPING]: download of lokal-album/CD1/cd1-track2
[SKIPPING]: download of lokal-album/CD1/cd1-track1
[STARTING]: download of lokal-album/CD2/cd2-track3
[FINISHED]: download of lokal-album/CD2/cd2-track3
[STARTING]: download of lokal-album/CD2/cd2-track2
[FINISHED]: download of lokal-album/CD2/cd2-track2
[STARTING]: download of lokal-album/CD2/cd2-track1
[FINISHED]: download of lokal-album/CD2/cd2-track1
Started: 3, Finished: 3, Failed: 0, Skipped: 3
```

**Windows**
```
sb-bitrepository-client\bin\sbclient.cmd -a download -f sumfil3 -c SB-devel-test1 -r album1\CD2\ -l lokal-album\CD2\
[SKIPPING]: download of lokal-album\CD1\cd1-track3
[SKIPPING]: download of lokal-album\CD1\cd1-track2
[SKIPPING]: download of lokal-album\CD1\cd1-track1
[STARTING]: download of lokal-album\CD2\cd2-track3
[FINISHED]: download of lokal-album\CD2\cd2-track3
[STARTING]: download of lokal-album\CD2\cd2-track2
[FINISHED]: download of lokal-album\CD2\cd2-track2
[STARTING]: download of lokal-album\CD2\cd2-track1
[FINISHED]: download of lokal-album\CD2\cd2-track1
Started: 3, Finished: 3, Failed: 0, Skipped: 3
```

Hvor efter der vil være en lokal mappe 'lokal-album' med følgende indhold:
```
lokal-album
│
├── CD2
│     ├─ cd2-track1  (Checksum: 297abb31e43c7b8ae947fa2a59fe9d40)
│     ├─ cd2-track2  (Checksum: 2a005b7d68bbb88374a4bfb6e53b5537)
│     ├─ cd2-track3  (Checksum: b4dfc4e7e4061a10f1cee2f030957733)
```

## Sletning af filer i bitmagasinet
For at slette filer i bitmagasinet anvendes 'delete' operationen. For at gardere imod at nogen ved et uheld kommer til at slette alle kopier af en eller flere filer, så kan slette operationen kun slette filer på én *pillar* af gangen. Ønsker man derfor at slette filer i en samling skal samme operation køres imod alle *pillars* i ens samling hver for sig. 

For at slette filerne vi uploadede tidligere kan 'sumfil2' anvendes til 'delete' operationen med følgende kommando:

**Linux**
```
sb-bitrepository-client/bin/sbclient.sh -a delete -f sumfil2 -c CD-collection -p pillar1
[STARTING]: delete of album1/CD1/cd1-track3
[FINISHED]: delete of album1/CD1/cd1-track3
[STARTING]: delete of album1/CD1/cd1-track2
[FINISHED]: delete of album1/CD1/cd1-track2
[STARTING]: delete of album1/CD1/cd1-track1
[FINISHED]: delete of album1/CD1/cd1-track1
[STARTING]: delete of album1/CD2/cd2-track3
[FINISHED]: delete of album1/CD2/cd2-track3
[STARTING]: delete of album1/CD2/cd2-track2
[FINISHED]: delete of album1/CD2/cd2-track2
[STARTING]: delete of album1/CD2/cd2-track1
[FINISHED]: delete of album1/CD2/cd2-track1
Started: 6, Finished: 6, Failed: 0, Skipped: 0
```

**Windows**
```
sb-bitrepository-client\bin\sbclient.cmd -a delete -f sumfil2 -c CD-collection -p pillar1
[STARTING]: delete of album1\CD1\cd1-track3
[FINISHED]: delete of album1\CD1\cd1-track3
[STARTING]: delete of album1\CD1\cd1-track2
[FINISHED]: delete of album1\CD1\cd1-track2
[STARTING]: delete of album1\CD1\cd1-track1
[FINISHED]: delete of album1\CD1\cd1-track1
[STARTING]: delete of album1\CD2\cd2-track3
[FINISHED]: delete of album1\CD2\cd2-track3
[STARTING]: delete of album1\CD2\cd2-track2
[FINISHED]: delete of album1\CD2\cd2-track2
[STARTING]: delete of album1\CD2\cd2-track1
[FINISHED]: delete of album1\CD2\cd2-track1
Started: 6, Finished: 6, Failed: 0, Skipped: 0
```

