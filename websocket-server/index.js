//TO START TYPE: nodemon index.js
//TO FORCE RESTART TYPE: rs

const app = require('express')();
const bodyParser = require('body-parser');
const { json } = require('express');
const server = require('http').createServer(app);
const io = require('socket.io')(server, {
  cors: {
    origin: "*",
    methods: ["GET", "POST"]
  }
});

connections = [];
gameConnections = [];
lobbyConnections = [];
menuConnections = [];
const chat = io.of('/chat');
const game = io.of('/game');
const lobby = io.of('/lobby');
const menu = io.of('/menu');


chat.on('connection', socket => {
  connections.push(socket);
  console.log('Chat: %s sockets connected', connections.length)

  socket.on('create', function (room) {
    socket.join(room);
    console.log("Chat socket joined room [" + room + "]")
  });

  socket.on('disconnect', function (data) {
    connections.splice(connections.indexOf(socket), 1);
    console.log('Chat: %s sockets connected', connections.length);
  });
});

game.on('connection', socket => {
  gameConnections.push(socket);
  console.log('Game: %s sockets connected', gameConnections.length)
  let gameroom;
  socket.on('create', function (room) {
    socket.join(room);
    gameroom=room;
    console.log("Game socket joined room [" + room + "]")
  });
  socket.on('gameTerminated',()=>{
    game.to(gameroom).emit("gameStoppedByHost");
  });
  socket.on('disconnect', function (data) {
    gameConnections.splice(connections.indexOf(socket), 1);
    console.log('Game: %s sockets connected', gameConnections.length);
  });
});

lobby.on('connection', socket => {
  let userRoom;
  lobbyConnections.push(socket);
  console.log('Lobby: %s sockets connected', lobbyConnections.length)
  socket.on('create', async function (room) {
    userRoom=room;
    socket.join(room);
    console.log("Lobby socket joined room [" + room + "]");
    const connectedSockets = await lobby.in(room).allSockets();
    if(connectedSockets!=undefined){
      console.log(connectedSockets.size);
      lobby.to(room).emit("userAmountChanged", connectedSockets.size);
    }
  });

  socket.on('disconnect', function (data) {
    lobbyConnections.splice(connections.indexOf(socket), 1);
    console.log('Lobby: %s sockets connected', lobbyConnections.length);
  });

  socket.on('startGame',function(data){
    console.log("data: "+data+" | userRoom: "+userRoom)
    lobby.to(userRoom).emit("goToGame",data);
  })

});

menu.on('connection', socket => {
  menuConnections.push(socket);
  console.log('Menu: %s sockets connected', menuConnections.length)

  socket.on('create', function (room) {
    socket.join(room);
    console.log("Menu socket joined room [" + room + "]")
  });

  socket.on('disconnect', function (data) {
    menuConnections.splice(menuConnections.indexOf(socket), 1);
    console.log('Menu: %s sockets connected', menuConnections.length);
  });
});

//CHAT FUNCTIONS
function reloadChatData(gameRoom) {
  chat.to(gameRoom).emit('reloadData');
}

//GAME FUNCTIONS
function reloadGameData(gameRoom) {
  console.log('sending reload to room ' + gameRoom)
  game.to(gameRoom).emit('reloadData');
}

function newTradeRequest(gameRoom, playerId){
  game.to(gameRoom).emit('newTradeRequest',playerId);
}

function newAchievement(gameRoom, playerId, achievement){
  game.to(gameRoom).emit('newAchievement', playerId, achievement);
}

function pauseGame(gameRoom){
  game.to(gameRoom).emit('pauseGame');
}

function refreshBoard(gameRoom){
  game.to(gameRoom).emit('refreshBoard');
}

function rolledSeven(gameRoom, playerId){
  game.to(gameRoom).emit('rolledSeven', playerId);
}

function discard(gameRoom, list){
  game.to(gameRoom).emit('discard',list);
}

function endGame(gameRoom, winnerId){
  game.to(gameRoom).emit('endGame', winnerId);
}

//LOBBY FUNCTIONS
function goToGame(lobbyRoom){
  lobby.to(lobbyRoom).emit('goToGame');
}

//MENU FUNCTIONS
function friendRequestInvite(menuRoom){
  menu.to(menuRoom).emit('friendRequestInvite');
}

server.listen(8082);
console.log("Sockets listening on port 8082.")


// REST API
app.use(bodyParser.json());
app.listen(8084, () =>
  console.log('REST listening on 8084'),
);

app.post('/trade', (req, res) => {
  console.log('Received POST for trade: game[' + req.body.gameId + '] player: ['+req.body.playerId+']');
  newTradeRequest(req.body.gameId, req.body.playerId);
  return res.send(true);
});

app.post('/achievement', (req, res)=>{
  console.log('Received POST for achievement: game[' + req.body.gameId + '] player: ['+req.body.playerId+'] achievement: ['+req.body.achievement+']');
  newAchievement(req.body.gameId, req.body.playerId, req.body.achievement);
  return res.send(true);
})

app.post('/endTurn', (req, res) => {
  console.log('Received POST for endTurn: game[' + req.body.gameId + ']');
  reloadGameData(req.body.gameId);
  return res.send(true);
});

app.post('/pauseGame', (req, res)=>{
  console.log('Received POST for pauseGame: game['+req.body.gameId+']');
  pauseGame(req.body.gameId);
  return res.send(true);
})

app.post('/goToGame', (req,res)=>{
  console.log('Received POST for goToGame: lobby['+req.body.lobbyId+']');
  goToGame(req.body.lobbyId);
  return res.send(true);
})

app.post('/newChatMessage', (req,res)=>{
  console.log('Received POST for newChatMessage: game['+req.body.gameId+']');
  reloadChatData(req.body.gameId);
  return res.send(true);
})

app.post('/refreshBoard', (req, res)=>{
  console.log('Received POST for refreshBoard: game['+req.body.gameId+']');
  refreshBoard(req.body.gameId);
  return res.send(true);
})

app.post('/rolledSeven', (req, res)=>{
  console.log('Received POST for rolledSeven: game['+req.body.gameId+'] | player : '+req.body.playerId);
  rolledSeven(req.body.gameId, req.body.playerId);
  return res.send(true);
})

app.post('/friendRequestInvite', (req, res)=>{
  console.log('Received POST for friendRequestInvite: user['+req.body.userId+']');
  friendRequestInvite(req.body.userId);
  return res.send(true);
})

app.post('/discard', (req, res)=>{
  console.log('Received POST for discard: game['+req.body.gameId+'] | list : '+req.body.list);
  discard(req.body.gameId, req.body.list);
  return res.send(true);
})

app.post('/endGame', (req,res)=>{
  console.log('Received POST for endGame: game['+req.body.gameId+'] | winner: '+req.body.winner);
  endGame(req.body.gameId, req.body.winner);
  return res.send(true);
})