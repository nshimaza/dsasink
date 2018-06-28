/**
 * @license
 * Copyright (c) 2016,2018 Cisco and/or its affiliates.
 *
 * This software is licensed to you under the terms of the Cisco Sample
 * Code License, Version 1.0 (the "License"). You may obtain a copy of the
 * License at
 *
 *                https://developer.cisco.com/docs/licenses
 *
 * All use of the material herein must be in accordance with the terms of
 * the License. All rights not expressly granted by the License are
 * reserved. Unless required by applicable law or agreed to separately in
 * writing, software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 */

package com.example

import java.io.{BufferedWriter, FileWriter}
import java.util.concurrent.SynchronousQueue

import org.dsa.iot.dslink.{DSLink, DSLinkFactory, DSLinkHandler}
import org.dsa.iot.dslink.node.value.SubscriptionValue
import org.dsa.iot.dslink.util.SubData
import org.dsa.iot.dslink.util.handler.Handler
import org.slf4j.LoggerFactory

/**
  * Created by nshimaza on 2016/09/19.
  */
object SinkDSLink {

  private val log = LoggerFactory.getLogger(getClass)
  private val finishMarker = new SynchronousQueue[Unit]()

  def main(args: Array[String]): Unit = {
    log.info("Starting Sink")

    val provider = DSLinkFactory.generate(args.drop(5),
      SinkDSLinkHandler(
        linkName = args(0),
        firstGen = args(1).toInt,
        lastGen = args(2).toInt,
        numNode = args(3).toInt,
        outDirName = args(4),
        markFinished = () => finishMarker.put(())
      )
    )
    provider.start()
    finishMarker.take()
    Thread.sleep(10000)
    println("exit Sink")
    System.exit(0)
  }
}

case class SinkDSLinkHandler(linkName: String,
                             firstGen: Int,
                             lastGen: Int,
                             numNode: Int,
                             outDirName: String,
                             markFinished: () => Unit
                            ) extends DSLinkHandler {

  private val log = LoggerFactory.getLogger(getClass)
  private val outFile = new BufferedWriter(new FileWriter(outDirName + s"$linkName$firstGen"))
  override def isRequester = true

  override def onRequesterInitialized(link: DSLink): Unit = {
    log.info("Sink initialized")
  }

  override def onRequesterConnected(link: DSLink): Unit = {
    log.info("Sink connected")

    for (i <- firstGen to lastGen;
         j <- 1 to numNode) {
      link.getRequester.subscribe(new SubData(s"/downstream/$linkName$i/c$j", 1), new Handler[SubscriptionValue] {
        def handle(event: SubscriptionValue): Unit = {
          val currTime = System.currentTimeMillis()
          val value = event.getValue
          val count = value.getNumber.intValue
          outFile.write(s"${event.getPath}, $count, ${currTime - value.getTime}\n")
          if (count == -1) {
            outFile.flush()
            markFinished()
          }
        }
      })
    }
  }
}
