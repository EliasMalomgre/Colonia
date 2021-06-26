# Integratie Project 2 - 2020-2021 - Front End - Colonia
Colonia is a version of 'Settlers of Catan' reimagined by a group of students at the Karel de Grote University College.

## Colonia
 - Arthur de Craemer
 - Daphne Deckers
 - Elias Malomgré
 - Louis Reyns
 - Tim Schelpe
 - Vink Van den Bosch

## Introduction
This project houses the front end for the Colonia Game Service. Utilising the Vue framework.

## Project Overview
Here is the general information about the project.
You can run the project with the following command:
> npm run serve

### Used technologies
- **Quasar** -> Component framework used throughout the entire project
    - Many Vue components 
    - Quasar Plugins -> Dialog & Notify
- **Vuex** -> Folder 'store' (index + different modules)
- **Axios** -> Folder 'services' (some are called in the store, others in Vue components)
- **Websockets** (socket.io, see Websocket service) -> In different Vue components, mainly these:
    - Home.vue (Menu sockets)
    - Game.vue (Game sockets)
    - Lobby.vue (Lobby sockets)
    - components/actiondrawercomponents/Chat.vue (Chat sockets)

### Data storage
User token is locally stored

### Images
The used favicon is stored in the public folder.
All other images used are locally stored in the src/assets folder (except for the quasar avatar). 
None of these images belong to us. Credit goes to the rightfull owners (see Google).

### Frontend testing
We have included some very limited scope tests. These tests are made using the _Jest_ framework.
They can be found in the \_\_tests\_\_ folder

tests can be run using the following command.
> npm test
