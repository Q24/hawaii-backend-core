/**
 * Copyright 2015 Q24
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
package io.kahu.hawaii.util.call.dispatch;

import org.apache.http.annotation.ThreadSafe;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@ThreadSafe
public class HawaiiBlockingQueue<E> implements BlockingQueue<E> {
    private final BlockingQueue<E> delegate;

    public HawaiiBlockingQueue(BlockingQueue<E> delegate) {
        this.delegate = delegate;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean add(E e) {
        return delegate.add(e);
    }

    @Override
    public Iterator<E> iterator() {
        return delegate.iterator();
    }

    @Override
    public E remove() {
        return delegate.remove();
    }

    @Override
    public boolean offer(E e) {
        return false;
    }

    @Override
    public E poll() {
        return delegate.poll();
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public E element() {
        return delegate.element();
    }

    @Override
    public E peek() {
        return delegate.peek();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    @Override
    public void put(E e) throws InterruptedException {
        delegate.put(e);
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.offer(e, timeout, unit);
    }

    @Override
    public E take() throws InterruptedException {
        return delegate.take();
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.poll(timeout, unit);
    }

    @Override
    public int remainingCapacity() {
        return delegate.remainingCapacity();
    }

    @Override
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return delegate.drainTo(c);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        return delegate.drainTo(c, maxElements);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return delegate.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

}
