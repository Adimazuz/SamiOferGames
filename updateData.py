import json
import requests
from bs4 import BeautifulSoup
from firebase import firebase

def auth_with_password(email, password):
    request_ref = 'https://auth.firebase.com/auth/firebase?firebase={0}&email={1}&password={2}'.\
        format(self.fire_base_name, email, password)
    request_object = self.requests.get(request_ref)
    return request_object.json()

URL = "https://www.haifa-stadium.co.il/%D7%9C%D7%95%D7%97_%D7%94%D7%9E%D7%A9%D7%97%D7%A7%D7%99%D7%9D_%D7%91%D7%90%D7%A6%D7%98%D7%93%D7%99%D7%95%D7%9F/"
firebase_url = 'https://samiofergames-default-rtdb.europe-west1.firebasedatabase.app/'
page = requests.get(URL)
soup = BeautifulSoup(page.content, "html.parser")
firebase = firebase.FirebaseApplication(firebase_url, None)



result = firebase.get('games', '')
job_elements = soup.find_all("div", class_="elementor-text-editor elementor-clearfix", partial=False)
games = []
if len(job_elements) > 3:
    result = firebase.delete(firebase_url, 'games')
    for i in range(0, len(job_elements) // 3):
        d = dict()
        d['team1'] = job_elements[i * 3].find('p').text
        d['team2'] = job_elements[i * 3 + 2].find('p').text
        date_and_time = job_elements[i * 3 + 1].findAll("p")[0].encode_contents().decode().strip().split('<br/>')
        d['date'] = date_and_time[0]
        d['time'] = date_and_time[1]
        result = firebase.post('/games', d)
