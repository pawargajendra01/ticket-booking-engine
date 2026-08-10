package com.gajendra.ticketbooking;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.gajendra.ticketbooking.datastructure.CustomPriorityQueue;

class CustomPriorityQueueTest {

    @Test
    void extractsElementsInAscendingOrder() {
        CustomPriorityQueue<Integer> heap = new CustomPriorityQueue<>();
        int[] input = {9, 3, 7, 1, 5, 2, 8, 4, 6, 0};
        for (int value : input) {
            heap.insert(value);
        }

        List<Integer> extracted = new ArrayList<>();
        while (!heap.isEmpty()) {
            extracted.add(heap.extractMin());
        }

        assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), extracted);
    }

    @Test
    void peekDoesNotRemoveElement() {
        CustomPriorityQueue<Integer> heap = new CustomPriorityQueue<>();
        heap.insert(10);
        heap.insert(5);

        assertEquals(5, heap.peek());
        assertEquals(2, heap.size());
    }

    @Test
    void handlesLargeRandomInputCorrectly() {
        CustomPriorityQueue<Integer> heap = new CustomPriorityQueue<>();
        List<Integer> reference = new ArrayList<>();
        java.util.Random random = new java.util.Random(42);

        for (int i = 0; i < 1000; i++) {
            int value = random.nextInt(10_000);
            heap.insert(value);
            reference.add(value);
        }
        reference.sort(Integer::compareTo);

        for (Integer expected : reference) {
            assertEquals(expected, heap.extractMin());
        }
        assertTrue(heap.isEmpty());
    }

    @Test
    void throwsWhenExtractingFromEmptyQueue() {
        CustomPriorityQueue<Integer> heap = new CustomPriorityQueue<>();
        assertThrows(IllegalStateException.class, heap::extractMin);
    }
}