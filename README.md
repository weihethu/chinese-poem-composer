chinese-poem-composer
=====================
##What is it

The composer automatically compose chinese poems by concantenating existing lines(����). It uses 
collocation extraction to ensure continuity between two adjacent lines and use topic models to ensure the overall
topic consistency. 

One example of ���� comes from the early Chinese Communist Party's leader qiubai qu(�����), 
and he composed this poem before he was killed:

Ϧ��������ɽ�У���Ҷ��Ȫ�����

������ٷʮ���£��ĳְ�����Ե�ա�

Each line of the above poem comes from different source poems, and they form a great poem when combined.
##How to run it
* clone this repository
* copy all text files to directory 'deployment': `cp *.txt deployment/`
* `cd deployment; java -jar composer.jar`

##Documents
See the documents in 'documents' sub-folder for user guides and how the composition algorithm works.
##About author
Wei He, a master student in Tsinghua university. This work is the course project of NLP.