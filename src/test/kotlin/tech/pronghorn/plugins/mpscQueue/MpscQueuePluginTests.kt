/*
 * Copyright 2017 Pronghorn Technology LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.pronghorn.plugins.mpscQueue

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.RepeatedTest
import tech.pronghorn.plugins.Consumer
import tech.pronghorn.plugins.Producer
import tech.pronghorn.plugins.mpmcQueue.MpmcQueuePlugin
import tech.pronghorn.test.PronghornTest
import tech.pronghorn.test.repeatCount
import tech.pronghorn.util.roundToNextPowerOfTwo

class MpscQueuePluginTests : PronghornTest() {
    /*
     * Tests the default mpsc queue plugin returns a bounded queue
     */
    @RepeatedTest(repeatCount)
    fun mpscQueuePluginBounded() {
        val capacity = roundToNextPowerOfTwo(4 + random.nextInt(64))
        val queue = MpscQueuePlugin.get<String>(capacity)

        var x = 0
        while (x < capacity) {
            queue.add("$x")
            x += 1
        }

        assertEquals(capacity, queue.size)
        assertFalse(queue.offer("foo"))
    }

    /*
     * Tests the default mpsc queue plugin is appropriately thread-safe
     */
    @RepeatedTest(repeatCount)
    fun mpscQueuePluginThreadSafe() {
        val capacity = 64
        val queue = MpmcQueuePlugin.get<Int>(capacity)

        val toConsume = capacity
        val toProduce = capacity / 2

        val consumer = Consumer(toConsume, queue)

        val producerA = Producer(toProduce, queue)
        val producerB = Producer(toProduce, queue)

        val threads = listOf(consumer, producerA, producerB)
        threads.forEach(Thread::start)
        threads.forEach { thread -> thread.join(1000) }

        assertEquals(toConsume, consumer.consumedCount)
        assertEquals(toProduce, producerA.producedCount)
        assertEquals(toProduce, producerB.producedCount)
    }
}
