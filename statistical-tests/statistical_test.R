## Load library
if (!require("devtools")) {
  install.packages("devtools")
}

if(!require("scmamp")){
  install.packages("scmamp")
}

if(!require("ggplot2")){
  install.packages("ggplot2")
}

if(!require("readxl")){
  install.packages("readxl")
}

library("scmamp")
library("devtools")
library("ggplot2")
library("readxl")

devtools::install_github("b0rxa/scmamp")

set.seed(0)
options(mc.cores = 8)
#rstan_options(auto_write = TRUE)

getCredibleIntervalsWeights <- function(posterior.samples, interval.size=0.9) {
  qmin <- (1-interval.size)/2
  qmax <- 1-qmin
  lower.bound <- apply(posterior.samples, MARGIN=2, FUN=quantile, p=qmin)
  upper.bound <- apply(posterior.samples, MARGIN=2, FUN=quantile, p=qmax)
  expectation <- apply(posterior.samples, MARGIN=2, FUN=mean)
  return (data.frame(Expected=expectation, Lower_bound=lower.bound, Upper_bound=upper.bound))
}


## Load data
#df <- read.csv("drflp.csv")
df <- read_excel("drflp.xlsx")

## Statistical Analysis
df[,2:length(df)] <- df[,2:length(df)] * -1
#df[,2:length(df)] <- df[,2:length(df)]
pl_model <-  bPlackettLuceModel(x.matrix=df[,2:length(df)], min=FALSE, nsim=4000, nchains=20, parallel=TRUE)
pl_model$expected.mode.rank
pl_model$expected.win.prob

## Plot
processed.results <- getCredibleIntervalsWeights(pl_model$posterior.weights, interval.size=0.9)
df <- data.frame(Algorithm=rownames(processed.results), processed.results)
ggplot(df, aes(y=Expected, ymin=Lower_bound, ymax=Upper_bound, x=Algorithm)) + geom_errorbar() + geom_point(col="darkgreen", size=2) +  theme_bw() + coord_flip() + labs(y="Probability of winning")
