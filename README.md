# sb-bitrepository-client

Original specs from SB IT operations:

makecheksum DIR SUMFIL
upload   COLL DIR SUMFIL [PREFIX]
                         fx 123\
Retry, uploader kun manglede på mindst et ben.

list     COLL TYPE [PREFIX]

download COLL DIR FIL_LISTE [PREFIX]
  PREFIX fjernes, tænk copy BITMAG:PREFIX/* DIR
Retry, henter kun manglede

delete   COLL PILLAR SUMFIL

Fejlhåndtering:
Forsøg nogle FÅ gange igen.
Ved mange fejl stop.
Raporter antal fejlede til slut.

download til temp, og rename.

-x droppes
-l client prefix
-r bitmag prefix
