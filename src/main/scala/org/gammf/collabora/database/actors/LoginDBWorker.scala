package org.gammf.collabora.database.actors

import akka.actor.{ActorRef, Stash}
import akka.pattern.pipe
import org.gammf.collabora.authentication.messages.LoginMessage
import org.gammf.collabora.database.messages.{GetConnectionMessage, InsertCollaborationMessage}
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global

class LoginDBWorker(connectionManager: ActorRef) extends UsersDBWorker(connectionManager) with Stash {


  override def receive: Receive = {
    case m: GetConnectionMessage =>
      connection = Some(m.connection)
      unstashAll()

    case _ if connection.isEmpty => stash()

    case message: LoginMessage => {
      getUsersCollection.map(users => users.find("_id" -> message.username).one[BSONDocument])
        .map(res => res.map(op => op.getOrElse(BSONDocument())))
        .recover({case _: Exception => BSONDocument()}) pipeTo sender
    }
  }
}
