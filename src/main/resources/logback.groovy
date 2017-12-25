import ch.qos.logback.classic.Level
import ch.qos.logback.core.*;
import ch.qos.logback.core.encoder.*;
import ch.qos.logback.core.read.*;
import ch.qos.logback.core.rolling.*;
import ch.qos.logback.core.status.*;
import ch.qos.logback.classic.net.*;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.INFO

encoderDefault = "%d{yyyy-MM-dd HH:mm:ss} %-5level [%logger{50}] %msg%n"
encoderAkka = "%d{yyyy-MM-dd HH:mm:ss} %-5level [%X{akkaSource}] %msg%n"

def mkConsole(name, encoderPattern) {
    appender(name, ConsoleAppender) {
        target = "System.out"
        encoder(PatternLayoutEncoder) {
            pattern = encoderPattern
        }
    }
}

def mkFile(name, encoderPattern, saveDay = 3) {
    def LOG_DIR = "${System.getProperty("user.dir")}/log"
    appender(name, RollingFileAppender) {
        rollingPolicy(TimeBasedRollingPolicy) {
            fileNamePattern = "${LOG_DIR}/$name/${name}-%d{yyyy-MM-dd}.log.zip"
            maxHistory = saveDay
        }
        encoder(PatternLayoutEncoder) {
            pattern = encoderPattern
        }
    }
}

class Conf {
    String name
    Level level = INFO
    int saveDay = 3
    String encoderPattern = "%d{yyyy-MM-dd HH:mm:ss} %-5level [%logger{50}] %msg%n"
}

def mkLogger(Conf conf) {
    mkFile(conf.name, conf.encoderPattern, conf.saveDay)
    logger(conf.name, conf.level, [conf.name])
}

mkLogger(new Conf(name: "akka", encoderPattern: encoderAkka, saveDay: 7))

mkConsole("CONSOLE", encoderDefault)
mkConsole("AkkaConsole", encoderAkka)
mkFile("total", encoderDefault)
mkLogger(new Conf(name:"akka",level: DEBUG))
mkLogger(new Conf(name: "client-actor", level: INFO))
root(INFO, ["CONSOLE", "total"])