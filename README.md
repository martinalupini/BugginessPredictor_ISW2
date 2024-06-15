
<div style="text-align: left;">
  <a href="https://sonarcloud.io/project/overview?id=martinalupini_BugginessPredictor_ISW2">
    <img src="https://sonarcloud.io/api/project_badges/measure?project=martinalupini_BugginessPredictor_ISW2&metric=code_smells" alt="Code Smells">
  </a>
</div>

<div style="text-align: right;">
  <img src="reportFiles/logo.png" width="80" height="80" alt="Project Logo">
</div>

--- 

# Bug Classification Performance Evaluator

The *Buggyness Predictor* is a specialized software tool designed 
to assess the effectiveness of various machine learning classifiers in predicting the 
likelihood of a software class containing bugs. It provides a 
comprehensive suite of functionalities to import datasets, 
apply multiple classifiers, and analyze their performance based 
on key metrics. The two software analyzed are Bookkeeper and Avro from Apache.

## Key features

- **Data import and preprocessing**: the data is retrieved using the combination between **Jira** and **Git**.
Missed information about the injected version and affected version are completed using **proportion**.  


- **Classifier support**: the classifiers analyzed are Random Forest, IBk and Naive Bayes. They are also combined with machine learning techniques such as feature selection, balancing and cost sensitive learning.  


- **Performance and effort-aware metrics**: Comprehensive evaluation metrics such as cost, precision, recall, F1-score, AUC and NPof30.

## Results

The results extracted from this evaluation can be found in the **Documentation/** folder.  
This tool is ideal for software developers, data scientists, and project managers looking to enhance their bug prediction models and improve software reliability. 

--- 
Final project for the course **Software Engineering 2**, University of Rome Tor Vergata (Master's Degree in  Computer Engineering), June 2024.

