#roda todos os makes da pasta test/
ls test/*.txt | sed 's/.txt//g' | sed 's#test\/##g' | xargs -I aa make aa | tail -1
