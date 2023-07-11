package com.info7255.demo.configuration;

public class TEST {

    String getBinary(int n)
    {
        StringBuilder sb;
        while(n>1)
        {
            sb.add(n%2);
            n=n/2;
        }
        sb.reverse();
    }

    int compare(String s1, String s2)
    {
        int length = s1.length();
        for(int i = length-1; i>=0; i--)
        {
            if(s1.charAt(i) != s2.charAt(i))
                return length - i;
        }
    }
}
