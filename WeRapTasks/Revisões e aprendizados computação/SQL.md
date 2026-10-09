
### CONSTRAINT 
Irá criar um nome a uma regra criada. Toda regra da tabela (PK, FK, UNIQUE, CHECK) é uma constraint.  A vantagem de dar o nome a uma regra é que:
-  as mensagens de erro são mais claras,
-  mais fácil trabalhar em migrations futuras 
-  mantem um padrão consistente

### Indices
Um índice facilita o postgres na consulta. Com ele, o SGBD criar uma estrutura ordenada e vai direto às linhas certas. Em tabelas com relações pequenas não tem muito impacto, mas em tabelas maiores, melhora o desempenho 

```
CREATE INDEX idx_name ON table(id_index)
``` 

### Chaves estrangeiras



