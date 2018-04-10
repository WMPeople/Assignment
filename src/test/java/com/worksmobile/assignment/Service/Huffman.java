package com.worksmobile.assignment.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.TreeMap;

/* Huffman coding , decoding */

public class Huffman {
    final boolean readFromFile = false;
    final boolean newTextBasedOnOldOne = false;

    PriorityQueue<Node> nodes = new PriorityQueue<>((o1, o2) -> (o1.value < o2.value) ? -1 : 1);
    TreeMap<Character, String> codes = new TreeMap<>();
    String text = "";
    String encoded = "";
    String decoded = "";
    int ASCII[] = new int[128];

    @Deprecated
    public void main(String[] args) throws FileNotFoundException {
        Scanner scanner = (readFromFile) ? new Scanner(new File("input.txt")) : new Scanner(System.in);
        int decision = 1;
        while (decision != -1) {
            if (handlingDecision(scanner, decision)) continue;
            decision = consoleMenu(scanner);
        }
    }

    @Deprecated
    private int consoleMenu(Scanner scanner) {
        int decision;
        System.out.println("\n---- Menu ----\n" +
                "-- [-1] to exit \n" +
                "-- [1] to enter new text\n" +
                "-- [2] to encode a text\n" +
                "-- [3] to decode a text");
        decision = Integer.parseInt(scanner.nextLine());
        if (readFromFile)
            System.out.println("Decision: " + decision + "\n---- End of Menu ----\n");
        return decision;
    }

    @Deprecated
    private boolean handlingDecision(Scanner scanner, int decision) {
        if (decision == 1) {
            if (handleNewText(scanner.nextLine())) return true;
        } else if (decision == 2) {
            if (handleEncodingNewText(scanner.nextLine())) return true;
        } else if (decision == 3) {
            handleDecodingNewText(scanner.nextLine());
        }
        return false;
    }

    /***
     * 현재 가지고 있는 코드 맵을 통하여 디코딩을 진행합니다.
     * @param encodedText 압축된 텍스트
     */
    public void handleDecodingNewText(String encodedText) {
        System.out.println("Enter the text to decode:");
        encoded = encodedText;
        System.out.println("Text to Decode: " + encoded);
        decodeText();
    }

    /***
     * 이전에 덧붙혀 인코딩을 진행합니다.
     * @param newPlainText 덧붙여질 새로운 평문 텍스트
     * @return 입력이 올바르지 않으면 true를 반환
     */
    public boolean handleEncodingNewText(String newPlainText) {
        System.out.println("Enter the text to encode:");
        text = newPlainText;
        System.out.println("Text to Encode: " + text);

        if (!IsSameCharacterSet()) {
            System.out.println("Not Valid input");
            text = "";
            return true;
        }
        encodeText();
        return false;
    }

    public boolean handleNewText(String plainText) {
        System.out.println("Enter the text:");
        text = plainText;
		ASCII = new int[128];
		nodes.clear();
		codes.clear();
		encoded = "";
		decoded = "";
		System.out.println("Text: " + text);
		calculateCharIntervals(nodes, true);
		buildTree(nodes);
		generateCodes(nodes.peek(), "");

		printCodes();
		System.out.println("-- Encoding/Decoding --");
		encodeText();
		decodeText();
		return false;
    }

    private boolean IsSameCharacterSet() {
        boolean flag = true;
        for (int i = 0; i < text.length(); i++)
            if (ASCII[text.charAt(i)] == 0) {
                flag = false;
                break;
            }
        return flag;
    }

    public String decodeText() {
        decoded = "";
        Node node = nodes.peek();
        for (int i = 0; i < encoded.length(); ) {
            Node tmpNode = node;
            while (tmpNode.left != null && tmpNode.right != null && i < encoded.length()) {
                if (encoded.charAt(i) == '1')
                    tmpNode = tmpNode.right;
                else tmpNode = tmpNode.left;
                i++;
            }
            if (tmpNode != null)
                if (tmpNode.character.length() == 1)
                    decoded += tmpNode.character;
                else
                    System.out.println("Input not Valid");

        }
        return decoded;
    }

    public String encodeText() {
        encoded = "";
        for (int i = 0; i < text.length(); i++)
            encoded += codes.get(text.charAt(i));
        return encoded;
    }

    private void buildTree(PriorityQueue<Node> vector) {
        while (vector.size() > 1)
            vector.add(new Node(vector.poll(), vector.poll()));
    }

    private void printCodes() {
        System.out.println("--- Printing Codes ---");
        codes.forEach((k, v) -> System.out.println("'" + k + "' : " + v));
    }

    private void calculateCharIntervals(PriorityQueue<Node> vector, boolean printIntervals) {
        if (printIntervals) System.out.println("-- intervals --");

        for (int i = 0; i < text.length(); i++)
            ASCII[text.charAt(i)]++;

        for (int i = 0; i < ASCII.length; i++)
            if (ASCII[i] > 0) {
                vector.add(new Node(ASCII[i] / (text.length() * 1.0), ((char) i) + ""));
                if (printIntervals)
                    System.out.println("'" + ((char) i) + "' : " + ASCII[i] / (text.length() * 1.0));
            }
    }

    private void generateCodes(Node node, String s) {
        if (node != null) {
            if (node.right != null)
                generateCodes(node.right, s + "1");

            if (node.left != null)
                generateCodes(node.left, s + "0");

            if (node.left == null && node.right == null)
                codes.put(node.character.charAt(0), s);
        }
    }
}

class Node {
    Node left, right;
    double value;
    String character;

    public Node(double value, String character) {
        this.value = value;
        this.character = character;
        left = null;
        right = null;
    }

    public Node(Node left, Node right) {
        this.value = left.value + right.value;
        character = left.character + right.character;
        if (left.value < right.value) {
            this.right = right;
            this.left = left;
        } else {
            this.right = left;
            this.left = right;
        }
    }
}