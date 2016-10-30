ADM
===

ADM is a tool for automation of toss for "anonymous Ded Moroz" game (russian 
analog of "Secret Santa"). Its just a simple study project written on 
[Kotlin language](http://kotlinlang.org/)

## Game rules

There must be at least three participants for ADM game. Each participant 
secretly gets (as a result of secret toss) name of another participant for 
whom he must prepare a gift. Participants must choose a date when they 
will present gifts each other (anonymously, of course).

## Building and using

Simply clone this repository and build project with maven. Then you can 
view usage message, just type:

```
$ java -jar jar-with-dependencies.jar -h
usage: adm [options] <csv_members_list>
 -c,--config <file>   Property file with configuration which overrides
                      standard config
```

After startup with correct arguments ADM tool will read specified csv 
files with game participants, mix list of all members, and send e-mail's 
with names to all participants.

### Configuration
For sending e-mail's ADM tool will take settings from specified properties 
file or from default configuration file. You can also set `from`, 
`subject`, and `deadline` properties in configuration files. See 
`default.properties` file for more info.