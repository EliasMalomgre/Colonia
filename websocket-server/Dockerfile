FROM node:latest
RUN npm install -g express ; sleep 20
WORKDIR /app
COPY package*.json ./
RUN npm install && mv /app/node_modules /node_modules
COPY . .
EXPOSE 8084 8082
CMD [ "node", "index.js" ]

