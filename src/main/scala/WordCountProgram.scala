

import scala.io.Source
import scala.util.Failure
import scala.util.Success
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern._
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global

class Parent extends Actor {
  val child = context.actorOf(Props[child], "child")
  var fileSend:Option[ActorRef]=None
  var wordCounts = 0
  def receive = {
    case "EOF" =>
      
      fileSend.map {_ ! wordCounts }
      
    case pathOfFile: String =>
      fileSend=Some(sender)
      for (line <- Source.fromFile(pathOfFile).getLines()) {
        
        child ! line
      }
      child ! "EOF"
    case countPerLine: Int => {
      wordCounts = wordCounts + countPerLine
    }
  }
}

class child extends Actor {
  var wordCountPerLine = 0
  def receive = {
    case "EOF" =>
      sender ! "EOF"
    case line: String =>
      {
        wordCountPerLine = line.split(" ").size
        sender ! wordCountPerLine
      }
  }
}


object WordCounter extends App {
  
  implicit val to=Timeout(5000)
  val system = ActorSystem("HelloSystem")
  val parent = system.actorOf(Props(new Parent), "parent")
  val file = "test.txt"
  val op = parent ? file
  op onComplete { 
  case Success(msg) => println("No of words =  "+msg)
  case Failure(f)=> println("fail"+f)
  }
  system.shutdown
}