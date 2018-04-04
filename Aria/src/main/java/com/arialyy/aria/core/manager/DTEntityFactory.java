/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arialyy.aria.core.manager;

import android.text.TextUtils;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTaskEntity;
import com.arialyy.aria.core.download.DownloadTaskWrapper;
import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.orm.DbEntity;
import java.io.File;
import java.util.List;

/**
 * Created by Aria.Lao on 2017/11/1.
 * 任务实体工厂
 */
class DTEntityFactory implements ITEntityFactory<DownloadEntity, DownloadTaskEntity> {
  private static final String TAG = "DTEntityFactory";
  private static volatile DTEntityFactory INSTANCE = null;

  private DTEntityFactory() {
  }

  public static DTEntityFactory getInstance() {
    if (INSTANCE == null) {
      synchronized (DTEntityFactory.class) {
        INSTANCE = new DTEntityFactory();
      }
    }
    return INSTANCE;
  }

  /**
   * 通过下载实体创建任务实体
   */
  private DownloadTaskEntity create(DownloadEntity entity) {
    List<DownloadTaskWrapper> wrapper = DbEntity.findRelationData(DownloadTaskWrapper.class,
        "DownloadTaskEntity.key=? and DownloadTaskEntity.isGroupTask='false' and DownloadTaskEntity.url=?",
        entity.getDownloadPath(), entity.getUrl());

    if (wrapper != null && !wrapper.isEmpty()) {
      DownloadTaskEntity taskEntity = wrapper.get(0).taskEntity;
      if (taskEntity == null) {
        taskEntity = new DownloadTaskEntity();
        taskEntity.entity = entity;
      } else if (taskEntity.entity == null || TextUtils.isEmpty(taskEntity.entity.getUrl())) {
        taskEntity.entity = entity;
      }

      return taskEntity;
    } else {
      DownloadTaskEntity taskEntity = new DownloadTaskEntity();
      taskEntity.entity = entity;
      return taskEntity;
    }
  }

  /**
   * 通过下载地址创建任务实体
   */
  @Override public DownloadTaskEntity create(String downloadUrl) {
    return create(getEntity(downloadUrl));
  }

  /**
   * 如果任务存在，但是下载实体不存在，则通过下载地址获取下载实体
   *
   * @param downloadUrl 下载地址
   */
  private DownloadEntity getEntity(String downloadUrl) {
    DownloadEntity entity =
        DownloadEntity.findFirst(DownloadEntity.class, "url=? and isGroupChild='false'",
            downloadUrl);
    if (entity == null) {
      entity = new DownloadEntity();
      entity.setUrl(downloadUrl);
      entity.setGroupChild(false);
    }
    File file = new File(entity.getDownloadPath());
    if (!file.exists()) {
      entity.setState(IEntity.STATE_WAIT);
    }
    return entity;
  }
}
