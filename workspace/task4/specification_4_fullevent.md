# Programmation événementielle

## Mise en place

Le but de ce nouveau projet est d'avoir un système événementiel. C'est-à-dire que l'ensemble du système fonctionne via des événements et sur un seul thread.
Actuellement, la tâche 3 reprend ce principe **seulement depuis la vue et programmation utilisateur**. Ici, nous allons l'appliquer à l'ensemble du système. L'API utilisateur reste donc inchangée par rapport à la tâche 3, à laquelle on peut se référer [ici](../task3/specification_3_event.md).  
L'ensemble des détails de la nouvelle implémentation (touchant donc aux couches inférieures de l'application non utilisée par l'utilisateur) sont détaillés dans le document [design du système](design_4_fullevent.md).
