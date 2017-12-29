# coding=utf-8
import cv2
import numpy as np
from PIL import Image , ImageDraw

def swap(point):
    return (point[1],point[0])

def im_draw(image,point,size=20):
    im = image
    draw =ImageDraw.Draw(im)
    draw.ellipse((point[0],point[1],point[0]+ size , point[1] + size), fill = (255, 0, 0))
    return im

def scale_16_9(hight):
    return (int(hight * 16 / 9) , hight )

def to_arr(image):
    return np.array(image)[:, :, 0:3]

def find_by_cvMatchTemplate(original,goal):
    result = cv2.matchTemplate(goal, original, cv2.TM_CCOEFF_NORMED)
    point = np.unravel_index(result.argmax(), result.shape)
    return (np.max(result), point[1], point[0])


def jvm_find_pic(original, goal, patten):

    if (type(goal) is str):
        goal = cv2.imread(goal)
    if (type(original) is str):
        original = cv2.imread(original)
    if (patten == "edge"):
        goal = cv2.Canny(goal, 10, 10)
        original = cv2.Canny(original, 10, 10)

    return str(find_by_cvMatchTemplate(original=original,goal=goal))


# jvm_find_pic("./screen.png",
#              "./images/wdj.png",
#              "default")
