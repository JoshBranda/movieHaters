/*
Copyright (c) 2018 Joshua Sander
This work is available under the "MIT License”.
Please see the file LICENSE in this distribution
for license terms.
*/

package com.example.joshuasander.moviehaters;


public class Review {

    private String uname;
    private double rating;
    private String review;

    public Review() {
    }

    public Review(String uname, double rating, String review) {
        this.uname = uname;
        this.rating = rating;
        this.review = review;
    }

    public String getUname(){return uname;}
    public double getRating(){return rating;}
    public String getReview(){return review;}
}
