//
//  main.cpp
//  FinalProject
//
//  Created by William Aldrich & Ted Pochmara on 9/18/17.
//  Copyright © 2017 William Aldrich/Ted Pochmara. All rights reserved.
//
// Textures & Sounds from www.opengameart.org, Authors: Michael Klier(sound) & Tatermand (art)
// Font from http://www.freefontspro.com/14454/arial.ttf

#include <stdio.h>
#include <iostream>
#include "World.hpp"

using namespace std;

int main(int argc, const char * argv[])
{
    srand((int)time(NULL));

    //start by creating a new world
    World();
    
    return 0;
}
