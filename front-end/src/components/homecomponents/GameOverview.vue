<template>
  <div class="q-pa-md">
    <q-btn-dropdown color="primary" label="Filter on status">
      <q-list>
        <q-item clickable v-close-popup @click="getGamesOverview('NONE')">
          <q-item-section>
            <q-item-label>NONE</q-item-label>
          </q-item-section>
        </q-item>

        <q-item clickable v-close-popup @click="getGamesOverview('PAUSED')">
          <q-item-section>
            <q-item-label>PAUSED</q-item-label>
          </q-item-section>
        </q-item>

        <q-item clickable v-close-popup @click="getGamesOverview('FINISHED')">
          <q-item-section>
            <q-item-label>FINISHED</q-item-label>
          </q-item-section>
        </q-item>
      </q-list>
    </q-btn-dropdown>
    <q-table
      title="Games"
      :data="data"
      :columns="columns"
      row-key="id"
    >
    <template v-slot:body-cell-action="props">
        <q-td :props="props" >
          <div v-if="props.row.state=='PAUSED'">
            <q-btn :label="props.value" @click="resumeGame(props.row.id)"></q-btn>
          </div>
        </q-td>
      </template>
    </q-table>
  </div>
</template>

<script>
import GameService from '../../services/game.service'
import {getLoggedInUser} from '../../services/player-methods'

export default {
  data () {
    return {
      columns: [
        {
          name: 'id',
          required: true,
          label: 'GameId',
          align: 'left',
          field: row => row.id,
          format: val => `${val}`,
          sortable: true
        },
        { name: 'amountOfPlayers', align: 'center', label: 'Amount of players', field: 'players', sortable: true },
        { name: 'playerVictoryPoints', label: 'Your victory points', field: 'victoryPoints', sortable: true },
        {name: 'state',label:'Game state',field: 'state',sortable:true},
        {name: 'action',label:'Resume game',field: 'action'}
      ],
      data: [
        {
          id: "TestGame",
          players: 2,
          victoryPoints: 3,
          state: "FINISHED"
        },
      ]
    }
  },
  created(){
      this.getGamesOverview("NONE");
  },
  methods:{
      getGamesOverview(filter){
          //reset table to prevent double data
          this.data=[]
          this.$store.action
            GameService.getGamesOverview(filter)
            .then((result)=>{
                //Create overview
                Object.values(result).forEach(el=>{
                    //Calculate this users VP in the game.
                    let user= getLoggedInUser();
                    let curUserVP=el.players.filter(p=>p.userId==user.id)[0].victoryPointsAmount
                    let amountAI=el.players.filter(p=>p.ai==true).length;
                    let gameAction="";
                    if(el.gameState=="PAUSED"){
                        gameAction="Resume"
                    }
                    let gamedata={
                        id: el.id,
                        players: ""+el.players.length+" ("+amountAI+" AI)",
                        victoryPoints: curUserVP,
                        state: el.gameState,
                        action: gameAction
                    }
                    this.data.push(gamedata);
                })
            });
      },
      resumeGame(gameID){
        GameService.resumeGame(gameID).then(result=>{
            console.log(result);
            this.$router.push({name: "Game", params: { id: gameID}})
        }).catch(()=>{
            this.$q.notify({
              type: 'negative',
              message: `Game could not be resumed `

            })
        })
      }
  }
}
</script>