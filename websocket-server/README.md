### Used technologies
- **Socket.io**
- **express**
    - used for rest server
        - uses json body-parser
    - used for socket server

### functionality description
The rest server receives calls from back-end micro services with socket room id's
and sometimes very basic information.
The socket server then emits these calls to the clients within the given room.

You can run the project with the following command:
> npm run start

### different sockets
- **Chat**
    - Receives rest from chat-service
    - Emits to front-end: Chat.vue
- **Game**
    - Receives rest from game-service
    - Emits to front-end: Game.vue
- **Lobby**
    - Receives socket emits from front-end: Lobby.vue
    - Emits to front-end: Lobby.vue
- **Menu**
    - Receives rest from user-service
    - Emits to front-end: Home.vue
