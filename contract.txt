pragma solidity >=0.4.22 <0.6.0;


contract Game {
    struct Player {
        uint pointCount; 
        bool end;  
        uint betTime; 
        uint betPrice;
        bool cheat;
        bool exist;
        address add;
    }
    
    
    struct Dealer {
        uint point;
        uint betTime;
    }
    
    address dealer;
    uint maxPlayer;
    uint playerCount;
    bool stop;
    bool endGame;
    Dealer abc;
    uint deposit_amount = 0;
    uint base_money = 0;
    uint total_bet = 0;
    
    
    mapping (address => Player) players;
    address []  addresses;
    Player [] allPlayers;
    Player [] loser;
    Player [] finalRound;
    Player [] cheaters;
    Player [] winners;
    
    
    
    constructor(uint256 max_player) public {
        abc = Dealer(0, 0);
        dealer = msg.sender;
        maxPlayer = max_player;
        playerCount =0;
        stop = false;
        endGame = false;
    }
    
    
    
    function Show_Deposite() pure public returns (string){
        return "Deposite for this game is 10";
    }
    
    
    function Add_BaseMoney_Dealer() public payable{
        // we,as dealer, will put 80 ether into the contract first
        //in case if we lost in the first round
        base_money = msg.value;
    }
    
    function Pay_Deposite_Player() public payable returns (string){
        if(msg.value!= 10**19){
            return "This is not the amount of requirement, please check the deposite amount by clicking Show_Deposite_Amount";
        } else {
            if(stop==true||endGame==true){
                msg.sender.transfer(msg.value);
                return;
            }else{
                //check player number
                if(playerCount==maxPlayer){
                    stop=true;
                    msg.sender.transfer(msg.value);
                    return;
                }else{
                    deposit_amount += msg.value;
                    return "Deposit complete";
                }
            }
 
        }
    }
    
    // enter value = bet before click this Join_The_Game
    function Join_The_Game_Player() public payable{
        //check game end?
        if(stop==true||endGame==true){
            msg.sender.transfer(msg.value);
            return;
        } 
        
        //check player number
        if(playerCount==maxPlayer){
            stop=true;
            msg.sender.transfer(msg.value);
            return;
        }
        
        
        if(!players[msg.sender].exist){
            
            players[msg.sender] = Player(0, false, 0, msg.value, false, true, msg.sender);
            
            total_bet += msg.value;

            addresses.push(msg.sender);
            playerCount++;
            return;
            
        }else{
            //mistaken click
            return;
        }
        
        
    }
    
    function Draw_Card() public{
        if(msg.sender == dealer){
            if(abc.betTime==5){
                return;
            } else {
                abc.point += random();
                abc.betTime += 1;
                return;
            }
        }else{
            if(players[msg.sender].exist){
                //cheater detect
                if(players[msg.sender].end==true||players[msg.sender].betTime>=5){
                    players[msg.sender].cheat = true;
                    players[msg.sender].end=true;
                    return;
                }
            players[msg.sender].pointCount += random();
            players[msg.sender].betTime +=1;
            return;
            }else{
                return;
            }
            
        }
        
    }
    
    function End_Bet_Player() public {
        if(!players[msg.sender].exist){
            return;
        }
        players[msg.sender].end = true;
    }
    
    
    
    function Check_If_End() private view returns (bool) {
        // return addresses.length;
        for(uint i=0;i<addresses.length;i++){
            if(players[addresses[i]].end==false){
                return false;
            }
        }
        
        return true;
        
     
    }
    
    function winner() private{
        for(uint i =0;i<finalRound.length;i++){
            if(finalRound[i].pointCount>=abc.point){
                winners.push(finalRound[i]);
            } else {
                loser.push(finalRound[i]);
            }
        }
    }
    
    function End_Game_Dealer() public{
        if(msg.sender == dealer){
            if(Check_If_End()){
                for(uint i = 0;i<addresses.length;i++){
                    if(players[addresses[i]].cheat){
                        cheaters.push(players[addresses[i]]);
                    } else {
                        if(players[addresses[i]].pointCount<=21){
                            finalRound.push(players[addresses[i]]);
                        }else {
                            loser.push(players[addresses[i]]);
                        }
                    }
                }
                
                winner();
                endGame = true;
                
                
            } else {
                return;
            }
        } else {
            return;
        }
            
    }
    
    
    //TODO: winner in winners array
    //loser in losers array
    // cheater in cheaters array
    
    function() public payable {
        // this function enables the contract to receive funds
    }
    
    function Final_Compute_Dealer() public payable{
        require(msg.sender == dealer);
        
        if(winners.length != 0){
            for (uint i=0; i< winners.length; i++) {
            //return deposit and bet and rewards to winner
            // return deposit
            
            address current_winner = winners[i].add;
            
            current_winner.transfer(10 ether);
            deposit_amount -= 10**19;
            // return bet and equal amount of rewards
            uint rewards = 2*(winners[i].betPrice);
            
            winners[i].add.transfer(rewards);
            
            total_bet -= winners[i].betPrice;
            base_money -= winners[i].betPrice;
            // funds -= rewards;
            // 1000000000000000000
            
            }
        }
        
        if(loser.length != 0){
            for (uint j=0; j< loser.length; j++) {
            //return deposit to winner
            
            address current_loser = loser[j].add;
            
            current_loser.transfer(10 ether);
            deposit_amount -= 10**19;
            }
        }
        
        if(cheaters.length != 0){
            for (uint k = 0; k < cheaters.length; k++) {
            uint punish = cheaters[k].betPrice;
                
            dealer.transfer(punish);
            total_bet -= punish;
            
            dealer.transfer(10 ether);
            deposit_amount -= 10**19;
            
            }
        }
        
        dealer.transfer(deposit_amount+total_bet+base_money);
        
        
    }
    
    
    
    function Point_Player () constant public returns (uint Player_Point){
        if(msg.sender==dealer){
            return;
        } else {
            return players[msg.sender].pointCount;
        }
    }
    
    function Rounds_Player () constant public returns (uint Player_Rounds){
        if(msg.sender==dealer){
            return;
        } else {
            return players[msg.sender].betTime;
        }
    }
    
    
    function Point_Dealer() constant public returns (uint Dealer_point) {
        return abc.point;
    }
    
    function Rounds_Dealer() constant public returns (uint Dealer_Rounds) {
        return abc.betTime;
    }
    

    function random() private view returns (uint256) {
        uint random = uint256(keccak256(block.timestamp, block.difficulty))%10;
        if(random == 0) return 1;
        return random;
    }
    
    

}
