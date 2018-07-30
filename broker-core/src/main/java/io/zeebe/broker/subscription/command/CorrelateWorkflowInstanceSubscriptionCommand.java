/*
 * Zeebe Broker Core
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.zeebe.broker.subscription.command;

import static io.zeebe.broker.subscription.CorrelateWorkflowInstanceSubscriptionDecoder.activityInstanceKeyNullValue;
import static io.zeebe.broker.subscription.CorrelateWorkflowInstanceSubscriptionDecoder.messageNameHeaderLength;
import static io.zeebe.broker.subscription.CorrelateWorkflowInstanceSubscriptionDecoder.payloadHeaderLength;
import static io.zeebe.broker.subscription.CorrelateWorkflowInstanceSubscriptionDecoder.workflowInstanceKeyNullValue;
import static io.zeebe.broker.subscription.CorrelateWorkflowInstanceSubscriptionDecoder.workflowInstancePartitionIdNullValue;

import io.zeebe.broker.subscription.CorrelateWorkflowInstanceSubscriptionDecoder;
import io.zeebe.broker.subscription.CorrelateWorkflowInstanceSubscriptionEncoder;
import io.zeebe.broker.util.SbeBufferWriterReader;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public class CorrelateWorkflowInstanceSubscriptionCommand
    extends SbeBufferWriterReader<
        CorrelateWorkflowInstanceSubscriptionEncoder,
        CorrelateWorkflowInstanceSubscriptionDecoder> {

  private final CorrelateWorkflowInstanceSubscriptionEncoder encoder =
      new CorrelateWorkflowInstanceSubscriptionEncoder();
  private final CorrelateWorkflowInstanceSubscriptionDecoder decoder =
      new CorrelateWorkflowInstanceSubscriptionDecoder();

  private int workflowInstancePartitionId;
  private long workflowInstanceKey;
  private long activityInstanceKey;

  private final UnsafeBuffer messageName = new UnsafeBuffer(0, 0);
  private final UnsafeBuffer payload = new UnsafeBuffer(0, 0);

  @Override
  protected CorrelateWorkflowInstanceSubscriptionEncoder getBodyEncoder() {
    return encoder;
  }

  @Override
  protected CorrelateWorkflowInstanceSubscriptionDecoder getBodyDecoder() {
    return decoder;
  }

  @Override
  public int getLength() {
    return super.getLength()
        + messageNameHeaderLength()
        + messageName.capacity()
        + payloadHeaderLength()
        + payload.capacity();
  }

  @Override
  public void write(MutableDirectBuffer buffer, int offset) {
    super.write(buffer, offset);

    encoder
        .workflowInstancePartitionId(workflowInstancePartitionId)
        .workflowInstanceKey(workflowInstanceKey)
        .activityInstanceKey(activityInstanceKey)
        .putMessageName(messageName, 0, messageName.capacity())
        .putPayload(payload, 0, payload.capacity());
  }

  @Override
  public void wrap(DirectBuffer buffer, int offset, int length) {
    super.wrap(buffer, offset, length);

    workflowInstancePartitionId = decoder.workflowInstancePartitionId();
    workflowInstanceKey = decoder.workflowInstanceKey();
    activityInstanceKey = decoder.activityInstanceKey();

    offset = decoder.limit();

    offset += messageNameHeaderLength();
    final int messageNameLength = decoder.messageNameLength();
    messageName.wrap(buffer, offset, messageNameLength);
    offset += messageNameLength;
    decoder.limit(offset);

    offset += payloadHeaderLength();
    final int paylaodLength = decoder.payloadLength();
    payload.wrap(buffer, offset, paylaodLength);
    offset += paylaodLength;
    decoder.limit(offset);
  }

  @Override
  public void reset() {
    workflowInstancePartitionId = workflowInstancePartitionIdNullValue();
    workflowInstanceKey = workflowInstanceKeyNullValue();
    activityInstanceKey = activityInstanceKeyNullValue();
    messageName.wrap(0, 0);
    payload.wrap(0, 0);
  }

  public int getWorkflowInstancePartitionId() {
    return workflowInstancePartitionId;
  }

  public void setWorkflowInstancePartitionId(int workflowInstancePartitionId) {
    this.workflowInstancePartitionId = workflowInstancePartitionId;
  }

  public long getWorkflowInstanceKey() {
    return workflowInstanceKey;
  }

  public void setWorkflowInstanceKey(long workflowInstanceKey) {
    this.workflowInstanceKey = workflowInstanceKey;
  }

  public long getActivityInstanceKey() {
    return activityInstanceKey;
  }

  public void setActivityInstanceKey(long activityInstanceKey) {
    this.activityInstanceKey = activityInstanceKey;
  }

  public DirectBuffer getMessageName() {
    return messageName;
  }

  public DirectBuffer getPayload() {
    return payload;
  }
}
