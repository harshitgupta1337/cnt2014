require(igraph)
bsk<-read.table("C:\\Users\\hp\\workspace\\CNT2014\\output\\new.net", sep='\t', dec=',', header=T)
bsk.network<-graph.data.frame(bsk, directed=F)
plot(bsk.network, layout=layout.fruchterman.reingold, vertex.label.cex = 0.7, vertex.size = 3, vertex.label.dist = 0.1, edge.width=(bsk)$grade, edge.color=E(bsk.network)$spec)