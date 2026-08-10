package com.gajendra.ticketbooking.datastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomPriorityQueue<T extends Comparable<T>> {

    private static final int DEFAULT_CAPACITY = 16;

    private Object[] heap;
    private int size;

    public CustomPriorityQueue() {
        this.heap = new Object[DEFAULT_CAPACITY];
        this.size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void insert(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Cannot insert null into CustomPriorityQueue");
        }
        ensureCapacity();
        heap[size] = value;
        siftUp(size);
        size++;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot peek an empty CustomPriorityQueue");
        }
        return (T) heap[0];
    }

    @SuppressWarnings("unchecked")
    public T extractMin() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot extract from an empty CustomPriorityQueue");
        }
        T min = (T) heap[0];
        size--;
        heap[0] = heap[size];
        heap[size] = null;
        if (size > 0) {
            siftDown(0);
        }
        return min;
    }

    @SuppressWarnings("unchecked")
    public List<T> toSortedList() {
        List<T> copy = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            copy.add((T) heap[i]);
        }
        Collections.sort(copy);
        return copy;
    }

    private void ensureCapacity() {
        if (size == heap.length) {
            Object[] bigger = new Object[heap.length * 2];
            System.arraycopy(heap, 0, bigger, 0, heap.length);
            heap = bigger;
        }
    }

    private void siftUp(int index) {
        while (index > 0) {
            int parent = parentOf(index);
            if (compare(index, parent) < 0) {
                swap(index, parent);
                index = parent;
            } else {
                break;
            }
        }
    }

    private void siftDown(int index) {
        while (true) {
            int left = leftChildOf(index);
            int right = rightChildOf(index);
            int smallest = index;

            if (left < size && compare(left, smallest) < 0) {
                smallest = left;
            }
            if (right < size && compare(right, smallest) < 0) {
                smallest = right;
            }
            if (smallest == index) {
                break;
            }
            swap(index, smallest);
            index = smallest;
        }
    }

    @SuppressWarnings("unchecked")
    private int compare(int i, int j) {
        return ((T) heap[i]).compareTo((T) heap[j]);
    }

    private void swap(int i, int j) {
        Object tmp = heap[i];
        heap[i] = heap[j];
        heap[j] = tmp;
    }

    private int parentOf(int index) {
        return (index - 1) / 2;
    }

    private int leftChildOf(int index) {
        return 2 * index + 1;
    }

    private int rightChildOf(int index) {
        return 2 * index + 2;
    }
}