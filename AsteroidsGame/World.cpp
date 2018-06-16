//
//  World.cpp
//  FinalProject
//
//  Created by William Aldrich & Ted Pochmara
//
//
#include "Planet.hpp"
#include "World.hpp"
#include "Player.hpp"
#include "Asteroids.hpp"
#include "Cosmos.hpp"
#include "Bullet.hpp"
#include <iostream>
#include <SFML/Graphics.hpp>
#include <SFML/Audio.hpp>
using namespace std;



World::World()
{
    //Create a 1800x1500 window cannot resize
    sf::RenderWindow newWorld(sf::VideoMode (1800,1500), "Whole new World", sf::Style::Close);
    
    newWorld.setFramerateLimit(60);
    
    //Create the Cosmos
    sf::Texture stars;
    stars.loadFromFile("images/stars.png");
    Cosmos cosmos(newWorld, stars);
    
    //Sexy sounds
    sf::Music music;
    music.openFromFile("images/music.ogg");
    music.play();
    
    //Loading pewpew into buffer; assign to variable and play in newBullet creation below
    sf::SoundBuffer buffer;
    buffer.loadFromFile("images/pewpew.wav");
    sf::Sound pewpew;
    pewpew.setBuffer(buffer);
    
    //Create Earth!
    sf::Texture globe;
    globe.loadFromFile("images/planetNew.png");
    Planet planet(globe, newWorld);
    
    //Create a new player at the start of a new world (position is set to 400x400 middle of window)
    sf::Texture fighter;
    fighter.loadFromFile("images/newShip.png");
    Player player(fighter, newWorld);
        
    //Text for the screen
    sf::Font font;
    font.loadFromFile("arial.ttf");
    
    sf::Text displayScore;
    setTextUp(displayScore, 300, 50, newWorld, font);
    
    sf::Text planetHitpoints;
    setTextUp(planetHitpoints, 460, 150, newWorld, font);
    
    sf::Text playerHitpoints;
    setTextUp(playerHitpoints, 460, 250, newWorld, font);
    
    //timers
    bulletTimer = 10, roidTimer = 0, healthTimer = 50;
    
    isPlaying = true;
    
    //do stuff while the window is actually open
    while (newWorld.isOpen())
    {
        cosmos.drawCosmos(newWorld);
        //need this to have the window run
        sf::Event event;
        while (newWorld.pollEvent(event))
        {
            // "close requested" event: we close the window
            if (event.type == sf::Event::Closed)
                newWorld.close();
        }

        //draw and display the ship and planet
        player.draw(newWorld);
        planet.draw(newWorld);

        //check if the player is moving and move the player
        player.isMoving(newWorld);
        
        //allows player to shoot with multiple projectiles
        if (player.shoot() && bulletTimer >= 12)
        {
            Bullet newBullet(player);
            pewpew.play();
            bulletsInWorld.push_back(newBullet);
            bulletTimer = 0;
        }
        
        //draw each bullet in the world
        if (isPlaying)
        {
            //send to make magic happen
            isPlayingGame(player, planet, newWorld, displayScore, planetHitpoints, playerHitpoints);
        }
        else
            gameOverScreen(newWorld, player);
        
        //display the world
        newWorld.display();

        //update the background if bullets are beyond the screen delete them
        remove(bulletsInWorld, newWorld);
        newWorld.clear(sf::Color::Black);
        bulletTimer++; roidTimer++; healthTimer++;
    }
}


//Allows Text to be set up
void setTextUp(sf::Text& text, const int& posX, const int& posY, const sf::RenderWindow& newWorld, const sf::Font& font)
{
    text.setCharacterSize(50);
    text.setFillColor(sf::Color::White);
    text.setStyle(sf::Text::Bold);
    text.setPosition(newWorld.getSize().x - posX, posY);
    text.setFont(font);
    
}

//If the game is not over comes here to complete
void World::isPlayingGame(Player& player, Planet& planet, sf::RenderWindow& newWorld, sf::Text& displayScore, sf::Text& planetHitpoints, sf::Text& playerHitpoints)
{
    sf::Texture asteroid3;
    asteroid3.loadFromFile("images/asteroid3.png");
    
    //draw all the bullets in the world
    for (int i = 0; i < bulletsInWorld.size(); i++)
        bulletsInWorld[i].draw(newWorld);
    
    //draw all the astroids in the world
    for (int i = 0; i < roidsInWorld.size(); i++)
        roidsInWorld[i].draw(newWorld);
    
    //make new asteroids on a timer
    if (roidsInWorld.size() < 6 && roidTimer == 60){
        Asteroids newAsteroid(asteroid3,newWorld);
        roidsInWorld.push_back(newAsteroid);
        roidTimer = 0;
    }
    
    //check to see if anything hits anything
    checkForHits(newWorld, player, planet);

    
    //display all the text.
    displayScore.setString("Score: " + to_string(player.getScore()));
    newWorld.draw(displayScore);
    planetHitpoints.setString("Planet Health: " + to_string(planet.planetLife));
    newWorld.draw(planetHitpoints);
    playerHitpoints.setString("Lives: " + to_string(player.getHitpoints()));
    newWorld.draw(playerHitpoints);
}



void World::checkForHits(sf::RenderWindow& newWorld, Player& player, Planet& planet)
{
    //look through each asteroid and check for collisions with planet or player
    for (int roids = 0; roids < roidsInWorld.size(); roids++){
        
        if (roidsInWorld[roids].getHitboxR().intersects(planet.getHitboxP())){
            roidsInWorld.erase(roidsInWorld.begin()+roids);
            planet.planetLife--;
            if(planet.planetLife <= 0)
            {
                gameOverScreen(newWorld, player);
                isPlaying = false;
            }
        }
        else if(roidsInWorld[roids].getHitboxR().intersects(player.getHitbox()) && healthTimer >= 50)
        {
            player.decreaseHitpoints();
            roidsInWorld.erase(roidsInWorld.begin()+roids);
            
            if(player.getHitpoints() <= 0)
            {
                gameOverScreen(newWorld, player);
                isPlaying = false;
            }
            healthTimer = 0;
        }
    }
    
    //if a bullet hits an asteroid, get rid of both and increase the player score
    for (int i = 0; i < bulletsInWorld.size(); i++)
    {
        for ( int j = 0; j < roidsInWorld.size(); j++)
        {
            if (bulletsInWorld[i].getHitboxB().intersects(roidsInWorld[j].getHitboxR()))
            {
                if(roidsInWorld[j].canDie)
                {
                    roidsInWorld.erase(roidsInWorld.begin()+j);
                }
                else
                {
                    roidsInWorld[j].resize();
                }
                bulletsInWorld.erase(bulletsInWorld.begin()+i);
                i--;
                j--;
                player.increaseScore();
            }
        }
    }
}


///If the game is over, comes here to complete
void gameOverScreen(sf::RenderWindow& newWorld, const Player& player)
{
    newWorld.clear(sf::Color::Black);
    
    sf::Font font;
    font.loadFromFile("arial.ttf");
    sf::Text gameOver;
    gameOver.setCharacterSize(150);
    gameOver.setFillColor(sf::Color::Red);
    gameOver.setStyle(sf::Text::Bold);
    gameOver.setFont(font);
    gameOver.setPosition(newWorld.getSize().x/2 - 400, newWorld.getSize().y/2-100);
    gameOver.setString("GAME OVER \n   Score: " + to_string(player.getScore()));
    newWorld.draw(gameOver);
}

